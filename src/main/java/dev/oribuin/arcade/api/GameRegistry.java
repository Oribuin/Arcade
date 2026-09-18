package dev.oribuin.arcade.api;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.api.game.GameInstance;
import dev.oribuin.arcade.manager.DataManager;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class GameRegistry {

    private final Map<String, Supplier<? extends ArcadeGame<?>>> registry = new HashMap<>();
    private final Map<UUID, ArcadeGame<?>> instances = new HashMap<>();
    private static GameRegistry instance;

    public GameRegistry() {
        instance = this;
    }

    public static GameRegistry get() {
        return instance;
    }

    /**
     * Register a new arcade game into the plugin
     *
     * @param identifier The identifier for the game
     * @param supplier   The function to get the game
     * @param <T>        The arcade game type
     */
    public static <T extends ArcadeGame<?>> Supplier<T> register(String identifier, @NotNull Supplier<T> supplier) {
        GameRegistry.get().getRegistry().put(identifier, supplier);

        DataManager dataManager = ArcadePlugin.getInstance().getDataManager();
        dataManager.loadGameInstances(identifier).thenAccept(instances -> {
            ArcadePlugin.getInstance().getLogger().info("Loading [" + instances.size() + "] instances of [" + identifier + "] into the world");
            for (GameInstance instance : instances) {
                PluginScheduler.get().runTaskAtLocation(instance.position(), () -> placeGame(
                        instance.name(),
                        instance.position(),
                        instance.direction()
                ));
            }
        });

        return supplier;
    }

    /**
     * Get the game instance of the
     *
     * @param instance The game instances
     * @param <T>      The type of game
     * @return The resulting game
     */
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T get(UUID instance) {
        return (T) GameRegistry.get().getInstances().get(instance);
    }

    /**
     * Check whether a game is already placed somewhere
     *
     * @param loc The location of the game
     * @return Whether a game is already placed somewhere
     */
    public static boolean isAlreadyPlaced(@NotNull Location loc) {
        return GameRegistry.get().getInstances().values().stream().anyMatch(x -> {
            Location pos = x.getLocation();
            boolean isSameWorld = pos.getWorld().equals(loc.getWorld());
            return isSameWorld
                    && pos.getBlockX() == loc.getBlockX()
                    && pos.getBlockY() == loc.getBlockY()
                    && pos.getBlockZ() == loc.getBlockZ();
        });
    }

    /**
     * Get the game that a user is currently participating in
     *
     * @param user The user potentially participating in the game
     * @param <T>  The type of game
     * @return The resulting game
     */
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T getParticipating(UUID user) {
        return GameRegistry.get().getInstances().values().stream()
                .filter(x -> x.getParticipants().containsKey(user))
                .map(x -> (T) x)
                .findFirst()
                .orElse(null);
    }

    /**
     * Place a specified arcade game into the world with a set direction
     *
     * @param name      The identifier of the game
     * @param position  The position of the game
     * @param direction The direction of the game
     * @param <T>       The arcade game being placed
     * @return The placed game into the world
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T placeGame(String name, Location position, BlockFace direction) {
        Supplier<? extends ArcadeGame<?>> game = GameRegistry.get().getRegistry().get(name);
        if (name == null) return null;

        ArcadeGame<?> result = game.get();
        result.place(position, direction);
        GameRegistry.get().getInstances().put(result.getIdentifier(), result);
        return (T) result;
    }

    public Map<String, Supplier<? extends ArcadeGame<?>>> getRegistry() {
        return registry;
    }

    public Map<UUID, ArcadeGame<?>> getInstances() {
        return instances;
    }

    //    /**
//     * Place an existing arcade game into the world with a set direction
//     *
//     * @param instance The instance data of the game
//     * @param <T>      The arcade game being placed
//     * @return The placed game into the world
//     */
//    @Nullable
//    @SuppressWarnings("unchecked")
//    public static <T extends ArcadeGame<?>> T placeInstance(@NotNull GameInstance instance) {
//        Supplier<? extends ArcadeGame<?>> game = REGISTRY.get(instance.name());
//        if (game == null) return null;
//
//        ArcadeGame<?> result = game.get();
//        result.setIdentifier(instance.identifier());
//        result.place(instance.position(), instance.direction());
//        GAME_INSTANCES.put(result.getIdentifier(), result);
//        return (T) result;
//    }


}
