package dev.oribuin.arcade.command;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.games.connectfour.token.TokenColour;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import dev.oribuin.arcade.util.ArcadeUtils;
import org.bukkit.Bukkit;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaceCommand {

    private final ArcadePlugin plugin;

    public PlaceCommand(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    @Command("arcade place <game> <duration>")
    @Permission("arcade.place")
    @CommandDescription("Places connect four down into the world")
    public void place(
            @NotNull Player sender,
            @Argument(value = "game", suggestions = "games") String game,
            @Argument("duration") Duration duration
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
            ArcadeGame<?> arcadeGame = GameRegistry.place(game.toLowerCase(), position, direction);
            if (arcadeGame == null) {
                sender.sendMessage("Could not find a game with a matching identifier");
                return;
            }

            Bukkit.getOnlinePlayers().forEach(arcadeGame::join);
            arcadeGame.start();
            PluginScheduler.get().runTaskAtLocationLater(
                    relative.getLocation(),
                    arcadeGame::unload,
                    duration.toSeconds(),
                    TimeUnit.SECONDS
            );
        });
    }

    @Suggestions("games")
    public List<String> gameSuggest(CommandContext<CommandSender> context, String input) {
        return GameRegistry.REGISTRY.keySet().stream().toList();
    }

    @Suggestions("tokens")
    public List<String> tokenSuggest(CommandContext<CommandSender> context, String input) {
        return new ArrayList<>(TokenColour.COLORS.keySet());
    }


}
