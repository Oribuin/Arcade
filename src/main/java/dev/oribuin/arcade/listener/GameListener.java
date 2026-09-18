package dev.oribuin.arcade.listener;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.game.ArcadeGame;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameListener implements Listener {

    private final ArcadePlugin plugin;

    public GameListener(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    // region General checks for all minigames

    /**
     * Remove any entities that are from a game that no longer has an instance
     *
     * @param event The chunk load event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoad(@NotNull ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            String gameId = container.get(ArcadeGame.GAME_ID, PersistentDataType.STRING);
            if (gameId == null) continue;

            // Remove any non-existent any instances
            if (!GameRegistry.get().getInstances().containsKey(UUID.fromString(gameId))) {
                entity.remove();
            }
        }
    }

    /**
     * Handle a listener check for whether the player has ragequit from the game
     *
     * @param event The player quit event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ArcadeGame<?> game = GameRegistry.getParticipating(player.getUniqueId());
        if (game == null || game.getLocation() == null) return;

        // Player has left the server, cancel the game
        game.leave(player, game.isActive());
    }

    /**
     * Handle a listener to check whether the player has teleported away from the game
     *
     * @param event The player teleport event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(@NotNull PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        ArcadeGame<?> game = GameRegistry.getParticipating(player.getUniqueId());
        if (game == null || game.getLocation() == null) return;

        boolean isSameWorld = event.getTo().getWorld().equals(event.getFrom().getWorld());
        boolean isTooFar = !isSameWorld || event.getFrom().distance(event.getTo()) >= 10;

        // Check if the player is 15 or more blocks away
        if (isTooFar) game.leave(player, game.isActive());
    }

    /**
     * Handle a listener check for whether the player has walked away from the game
     *
     * @param event The player move event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location destination = event.getTo();

        // Check if there are any games
        if (GameRegistry.get().getInstances().isEmpty()) return;

        // Player has to physically move their body, not just their head
        if (from.getBlockX() != destination.getBlockX() || from.getBlockY() != destination.getBlockY() || from.getBlockZ() != destination.getBlockZ()) {
            return;
        }

        // TODO: Hide players who are inside the game bounds that are not participating to prevent distractions
        ArcadeGame<?> game = GameRegistry.getParticipating(player.getUniqueId());
        if (game == null || game.getLocation() == null) return;

        // Check if the player is 15 or more blocks away
        if (destination.distance(game.getLocation()) > 10) {
            game.leave(player, game.isActive()); // Handle the leaving of the game
        } else {
            if (game.isActive()) game.handleEvent(event); // Handle move event for the player
        }
    }
    // endregion

    /**
     * Handle interacting of the game
     *
     * @param event The game to interact with
     */
    @EventHandler
    public void onEntityInteract(@NotNull PlayerInteractEntityEvent event) {
        // okay yesn't on this one 
        Player player = event.getPlayer();
        PersistentDataContainer container = event.getRightClicked().getPersistentDataContainer();
        String gameId = container.get(ArcadeGame.GAME_ID, PersistentDataType.STRING);
        if (gameId == null) return;

        ArcadeGame<?> game = GameRegistry.get(UUID.fromString(gameId));
        if (game == null || game.getLocation() == null) return;

        // Handle the event for the player participating
        if (game.getParticipants().containsKey(player.getUniqueId())) {
            // handle the interact event for the player
            if (game.isActive()) {
                game.handleEvent(event);
            }
            return;
        }

        game.join(event.getPlayer());
    }
    // region Events that are handled by the player
    // endregion
}
