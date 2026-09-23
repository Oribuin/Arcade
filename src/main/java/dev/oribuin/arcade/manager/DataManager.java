package dev.oribuin.arcade.manager;

import com.google.gson.Gson;
import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.event.GameRegistrationEvent;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.api.game.GameInstance;
import dev.oribuin.arcade.config.DatabaseSettings;
import dev.oribuin.arcade.database.connector.DatabaseConnector;
import dev.oribuin.arcade.database.connector.MySQLConnector;
import dev.oribuin.arcade.database.connector.SQLiteConnector;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import dev.oribuin.arcade.statistic.GameStats;
import dev.oribuin.arcade.statistic.StatWrapper;
import dev.oribuin.arcade.util.ArcadeUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataManager implements Manager {

    private static final Gson GSON = new Gson();

    private final ArcadePlugin plugin;
    private final Map<UUID, StatWrapper> stats;
    private DatabaseConnector connector;

    public DataManager(@NotNull ArcadePlugin plugin) {
        this.plugin = plugin;
        this.stats = new HashMap<>();
    }

    /**
     * The task that runs when the plugin is loaded/reloaded
     *
     * @param plugin The plugin reloading
     */
    @Override
    public void reload(ArcadePlugin plugin) {
        this.disable(plugin);

        DatabaseSettings sqlConfig = DatabaseSettings.get();
        if (sqlConfig.isEnabled()) {
            String hostname = sqlConfig.getHostname();
            int port = sqlConfig.getPort();
            String database = sqlConfig.getDatabaseName();
            String username = sqlConfig.getUsername();
            String password = sqlConfig.getPassword();
            boolean useSSL = sqlConfig.useSSL();
            int poolSize = sqlConfig.getConnectionPoolSize();

            this.connector = new MySQLConnector(this.plugin, hostname, port, database, username, password, useSSL, poolSize);
            this.plugin.getLogger().info("Data manager connected using MySQL.");
        } else {
            this.connector = new SQLiteConnector(this.plugin);
            this.connector.cleanup();
            this.plugin.getLogger().info("Data manager connected using SQLite.");
        }

        CompletableFuture.runAsync(() -> this.connector.connect(connection -> {
            this.plugin.getLogger().info("Registering database tables");
            // Create the initial tables for the plugin
            try (Statement statement = connection.createStatement()) {
                statement.addBatch(CREATE_TABLE_GAMES);
                statement.addBatch(CREATE_TABLE_STATS);
                statement.executeBatch();
            }
            
            // Loads all the games within the plugin
        })).thenRun(() -> new GameRegistrationEvent().callEvent()).thenRunAsync(() -> {
            // Loads the user's stats in the plugin that are currently online
            Bukkit.getOnlinePlayers().forEach(player -> {
                StatWrapper wrapper = this.loadStatsUnsafe(player.getUniqueId());
                this.stats.put(player.getUniqueId(), wrapper);
            });
        });
    }

    /**
     * Load the instances of a specified game type
     *
     * @param id The id of the game
     * @return The resulting games
     * @see dev.oribuin.arcade.api.GameRegistry#register(String, Supplier)
     */
    public CompletableFuture<List<GameInstance>> loadGameInstances(String id) {
        return CompletableFuture.supplyAsync(() -> {
            List<GameInstance> results = new ArrayList<>();
            this.connector.connect(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(SELECT_GAMETYPES)) {
                    statement.setString(1, id);
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) results.add(this.construct(resultSet));
                }
            });

            return results;
        });
    }

    /**
     * Save a game instance into the plugin
     *
     * @param game The game to save
     * @param <T>  The game type
     */
    public <T extends ArcadeGame<?>> void saveGame(T game) {
        this.async(() -> this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(SAVE_GAME)) {
                statement.setString(1, game.getName());
                statement.setString(2, game.getLocation().getWorld().key().asString());
                statement.setDouble(3, game.getLocation().x());
                statement.setDouble(4, game.getLocation().y());
                statement.setDouble(5, game.getLocation().z());
                statement.setString(6, game.getDirection().name());
                statement.executeUpdate();
            }
        }));
    }


    /**
     * Remove a game instance from the plugin
     *
     * @param game The game to remove
     * @param <T>  The game type
     */
    public <T extends ArcadeGame<?>> void removeGame(T game) {
        this.async(() -> this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(REMOVE_GAME)) {
                statement.setString(1, game.getName());
                statement.setString(2, game.getLocation().getWorld().key().asString());
                statement.setDouble(3, game.getLocation().x());
                statement.setDouble(4, game.getLocation().y());
                statement.setDouble(5, game.getLocation().z());
                statement.executeUpdate();
            }

        }));
    }

    /**
     * Update a user's current stats with a consumer
     *
     * @param user     The user to update
     * @param game     The game to update the stats for
     * @param consumer The functionality to apply to the stat
     * @param <T>      The type of game being updated
     */
    public <T extends ArcadeGame<?>> void updateStat(@NotNull UUID user, @NotNull T game, @NotNull Consumer<GameStats> consumer) {
        StatWrapper wrapper = this.stats.computeIfAbsent(user, x -> new StatWrapper());
        GameStats gameStats = wrapper.stats().getOrDefault(game.getName(), new GameStats());
        consumer.accept(gameStats);
        this.writeStat(user, game.getName(), gameStats);
    }
    
    /**
     * Update a user's current stats with a consumer
     *
     * @param user     The user to update
     * @param game     The game to update the stats for
     * @param consumer The functionality to apply to the stat
     */
    public void updateStat(@NotNull UUID user, @NotNull String game, @NotNull Consumer<GameStats> consumer) {
        StatWrapper wrapper = this.stats.computeIfAbsent(user, x -> new StatWrapper());
        GameStats gameStats = wrapper.stats().getOrDefault(game, new GameStats());
        consumer.accept(gameStats);
        this.writeStat(user, game, gameStats);
    }

    /**
     * Write  user's stats into the database & cache
     *
     * @param user  The user to update
     * @param game  The game with the associated stats
     * @param stats The stats to write
     */
    public void writeStat(@NotNull UUID user, @NotNull String game, @NotNull GameStats stats) {
        StatWrapper wrapper = this.stats.computeIfAbsent(user, x -> new StatWrapper());
        wrapper.stats().put(game, stats);
        this.stats.put(user, wrapper);

        this.async(() -> this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(SAVE_STATS)) {
                statement.setString(1, user.toString());
                statement.setString(2, GSON.toJson(wrapper));
                statement.executeUpdate();
            }
        }));
    }

    /**
     * Load a user's stats on for each game
     *
     * @param user The user to load
     * @return The user's stats to load
     */
    private StatWrapper loadStatsUnsafe(@NotNull UUID user) {
        try (Connection connection = this.connector.connect();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_STATS)
        ) {
            statement.setString(1, user.toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return GSON.fromJson(set.getString("stats"), StatWrapper.class);
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().severe("An error occurred executing an SQLite query: " + ex.getMessage());
        }

        return new StatWrapper();
    }

    /**
     * Load a user's stats on for each game
     *
     * @param user The user to load
     * @return The user's stats to load
     */
    public CompletableFuture<StatWrapper> loadStats(UUID user) {
        return CompletableFuture.supplyAsync(() -> loadStatsUnsafe(user));
    }

    /**
     * Get the combined statistics of all the player's stuff
     *
     * @param user The user to get the stats for 
     * @return The resulting game stats
     */
    @NotNull
    public GameStats getCombined(@NotNull UUID user) {
        StatWrapper wrapper = this.stats.computeIfAbsent(user, x -> new StatWrapper());
        GameStats gameStats = new GameStats();

        GameRegistry.get().getRegistry().keySet().forEach(s -> wrapper.stats().values().forEach(x -> {
            gameStats.setWins(gameStats.getWins() + x.getWins());
            gameStats.setLosses(gameStats.getLosses() + x.getLosses());
            gameStats.setPlays(gameStats.getPlays() + x.getPlays());
        }));

        return gameStats;
    }

    @NotNull
    public GameStats getStats(@NotNull UUID user, @NotNull String game) {

        // Get the combined stats if the game is combined
        if (game.equalsIgnoreCase("combined")) return this.getCombined(user);

        StatWrapper wrapper = this.stats.computeIfAbsent(user, x -> new StatWrapper());
        GameStats gameStats = wrapper.stats().get(game);

        // User's stats are already cached
        if (gameStats != null) return gameStats;

        // load the user's stats
        this.loadStats(user).thenAccept(x -> this.stats.put(user, x));

        return new GameStats();
    }

    /**
     * Construct a game instance into a plugin
     *
     * @param set The result set to load the instance from
     * @return The resulting game instance
     * @throws SQLException Any SQL Exceptions that may occur
     */
    public GameInstance construct(@NotNull ResultSet set) throws SQLException {
        String name = set.getString("name");
        @Subst("world:overworld")
        String world = set.getString("world");
        double x = set.getDouble("position_x");
        double y = set.getDouble("position_y");
        double z = set.getDouble("position_z");
        BlockFace direction = ArcadeUtils.getEnum(BlockFace.class, set.getString("direction"));
        Location position = new Location(Bukkit.getWorld(Key.key(world)), x, y, z);
        return new GameInstance(name, position, direction);
    }

    /**
     * The task that runs when the plugin is disabled, usually takes priority over {@link Manager#reload(ArcadePlugin)}
     *
     * @param plugin The plugin being disabled
     */
    @Override
    public void disable(ArcadePlugin plugin) {
        if (this.connector != null) {
            // Wait for all connections to finish
            long now = System.currentTimeMillis();
            long deadline = now + 5000;
            synchronized (this.connector.getLock()) {
                while (!this.connector.isFinished() && now < deadline) {
                    try {
                        this.connector.getLock().wait(deadline - now);
                        now = System.currentTimeMillis();
                    } catch (InterruptedException ex) {
                        this.plugin.getLogger().severe("Interrupted error occurred: " + ex.getMessage());
                    }
                }
            }

            this.connector.closeConnection();
        }
    }

    /**
     * Run a task asynchronously
     *
     * @param runnable The task to run
     */
    public void async(Runnable runnable) {
        PluginScheduler.get().runTaskAsync(runnable);
    }

    public Map<UUID, StatWrapper> getStats() {
        return stats;
    }

    // region SQL Queries
    private final String CREATE_TABLE_GAMES = "CREATE TABLE IF NOT EXISTS `arcadeplugin_games` (" +
            "`name` VARCHAR(64) NOT NULL," +
            "`world` VARCHAR(64) NOT NULL," +
            "`position_x` DOUBLE NOT NULL," +
            "`position_y` DOUBLE NOT NULL," +
            "`position_z` DOUBLE NOT NULL," +
            "`direction` VARCHAR(64) NOT NULL" +
            ")";

    private final String CREATE_TABLE_STATS = "CREATE TABLE IF NOT EXISTS `arcadeplugin_stats` (" +
            "`player` VARCHAR(36) NOT NULL PRIMARY KEY, " +
            "`stats` VARCHAR(2048) NOT NULL" +
            ")";

    private final String SAVE_GAME = "REPLACE INTO `arcadeplugin_games` " +
            "(`name`, `world`, `position_x`, `position_y`, `position_z`, `direction`) " +
            "VALUES(?, ?, ?, ?, ?, ?)";

    private final String SAVE_STATS = "REPLACE INTO `arcadeplugin_stats` " +
            "(`player`, `stats`) VALUES (?, ?)";

    private final String SELECT_USER_STATS = "SELECT `stats` FROM `arcadeplugin_stats` WHERE `player` = ?";

    private final String SELECT_GAMETYPES = "SELECT * FROM `arcadeplugin_games` WHERE `name` = ?";

    private final String REMOVE_GAME = "DELETE FROM `arcadeplugin_games` WHERE " +
            "`name` = ? AND " +
            "`world` = ? AND " +
            "`position_x` = ? AND " +
            "`position_y` = ? AND " +
            "`position_z` = ?";
    // endregion
}
