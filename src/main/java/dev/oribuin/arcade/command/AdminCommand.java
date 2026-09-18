package dev.oribuin.arcade.command;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import dev.oribuin.arcade.util.ArcadeUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminCommand {

    private final ArcadePlugin plugin;

    public AdminCommand(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Places an arcade game with a given name into the world
     *
     * @param sender The command sender
     * @param game   The game to place
     */
    @Command("arcade admin place <game>")
    @Permission("arcade.admin")
    @CommandDescription("Places connect four down into the world")
    public void place(@NotNull Player sender, @Argument(value = "game", suggestions = "games") String game) {
        PluginScheduler.get().runTaskAtLocation(sender.getLocation(), () -> {
            Block target = sender.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                sender.sendMessage("You need to be looking at a block");
                return;
            }

            // Get the target block of the player
            Block relative = target.getRelative(BlockFace.UP);
            if (relative.getType() != Material.AIR) {
                sender.sendMessage("There needs to be an empty space above the block");
                return;
            }

            BlockFace direction = sender.getFacing();
            Location position = relative.getLocation().toCenterLocation().clone();

            // Check whether there is already a game there
            if (GameRegistry.isAlreadyPlaced(position)) {
                sender.sendMessage("There is already a game at this location");
                return;
            }

            // Place the game into the world
            ArcadeGame<?> arcadeGame = GameRegistry.placeGame(game.toLowerCase(), position, direction);
            if (arcadeGame == null) {
                sender.sendMessage("Could not find a game with a matching identifier");
                return;
            }

            ArcadePlugin.getInstance().getDataManager().saveGame(arcadeGame);
            sender.sendMessage("You have placed a game of " + ArcadeUtils.niceify(game));
        });
    }

    /**
     * Removes the targeted arcade game from the world
     *
     * @param sender The command sender
     */
    @Command("arcade admin remove")
    @Permission("arcade.admin")
    @CommandDescription("Removes a game from the world")
    public void remove(@NotNull Player sender) {
        PluginScheduler.get().runTaskAtEntity(sender, () -> {
            Entity target = sender.getTargetEntity(5);
            if (target == null) {
                sender.sendMessage("You need to be looking at a game");
                return;
            }

            PersistentDataContainer container = target.getPersistentDataContainer();
            String gameId = container.get(ArcadeGame.GAME_ID, PersistentDataType.STRING);
            if (gameId == null) {
                sender.sendMessage("This entity is not part of a game");
                return;
            }

            ArcadeGame<?> game = GameRegistry.get(UUID.fromString(gameId));
            if (game == null || game.getLocation() == null) {
                sender.sendMessage("We couldn't find the game you are looking for");
                return;
            }
            
            game.unload();
            GameRegistry.get().getInstances().remove(game.getIdentifier());
            this.plugin.getDataManager().removeGame(game);
            sender.sendMessage("You have removed a game of " + game.getName() + " from the world");
        });
    }

    /**
     * Removes all the games of a specified type
     *
     * @param sender The command sender
     */
    @Command("arcade admin removeall [game]")
    @Permission("arcade.admin")
    @CommandDescription("Removes all game types from a world")
    public void removeAll(@NotNull Player sender, @Nullable @Argument(value = "game", suggestions = "games") String gameId) {

        int total = 0;
        for (ArcadeGame<?> game : new ArrayList<>(GameRegistry.get().getInstances().values())) {
            if (game.getLocation() == null) continue;
            if (gameId != null && !game.getName().equalsIgnoreCase(gameId)) continue;

            total++;
            GameRegistry.get().getInstances().remove(game.getIdentifier());
            this.plugin.getDataManager().removeGame(game);
            PluginScheduler.get().runTaskAtLocation(game.getLocation(), game::unload);
        }

        sender.sendMessage("You have deleted [" + total + "] games");
    }

    @Suggestions("games")
    public List<String> gameSuggest(CommandContext<CommandSender> context, String input) {
        return GameRegistry.get().getRegistry().keySet().stream().toList();
    }

}
