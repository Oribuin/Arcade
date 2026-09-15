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
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceCommand {

    private final ArcadePlugin plugin;

    public PlaceCommand(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    @Command("arcade place <game>")
    @Permission("arcade.place")
    @CommandDescription("Places connect four down into the world")
    public void place(
            @NotNull Player sender,
            @Argument(value = "game", suggestions = "games") String game
    ) {
        PluginScheduler.get().runTaskAtLocation(sender.getLocation(), () -> {
            Block target = sender.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                sender.sendMessage("You need to be looking at a block");
                return;
            }

            Block relative = target.getRelative(BlockFace.UP);
            if (relative.getType() != Material.AIR) {
                sender.sendMessage("There needs to be an empty space above the block");
                return;
            }

            BlockFace direction = sender.getFacing();
            Location position = relative.getLocation().toCenterLocation().clone();
            ArcadeGame<?> arcadeGame = GameRegistry.placeFreshGame(game.toLowerCase(), position, direction);
            if (arcadeGame == null) {
                sender.sendMessage("Could not find a game with a matching identifier");
                return;
            }

            sender.sendMessage("You have placed a game of " + ArcadeUtils.niceify(game));
        });
    }

    @Suggestions("games")
    public List<String> gameSuggest(CommandContext<CommandSender> context, String input) {
        return GameRegistry.REGISTRY.keySet().stream().toList();
    }

}
