package dev.oribuin.arcade.api.game;

import dev.oribuin.arcade.api.participant.Participant;
import dev.oribuin.arcade.config.Messages;
import dev.oribuin.arcade.util.ArcadeUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class ArcadeGame<T extends Participant> implements ForwardingAudience.Single {

    protected final String identifier; // The identifier for the game 
    protected final Map<UUID, T> participants; // The people playing the game
    protected final double wager; // The money placed within the game 
    protected final int playerCount;  // The amount of people required to play the game
    protected Location location; // The centre location of the game
    protected boolean active;

    /**
     * Creates a new arcade game for the plugin
     *
     * @param identifier The identifier for the game
     */
    public ArcadeGame(String identifier) {
        this.identifier = identifier;
        this.participants = new HashMap<>();
        this.playerCount = 2;
        this.wager = 0.0;
        this.active = false;
    }

    /**
     * Functionality provided for when the game is placed down in the world
     *
     * @param location  The location of the game
     * @param direction The direction the player was facing
     */
    public abstract void place(@NotNull Location location, @NotNull BlockFace direction);

    /**
     * A request from a player to join a game
     *
     * @param player The player who sent the game
     */
    public boolean join(Player player) {
        // Check whether the game is already running
        if (this.active) {
            Messages.get().getAlreadyActive().send(player);
            return false;
        }

        // The location of the player
        if (this.location == null) {
            Messages.get().getUnknownPosition().send(player);
            return false;
        }

        // Check whether the player has already joined
        if (this.participants.containsKey(player.getUniqueId())) {
            Messages.get().getAlreadyJoined().send(player);
            return false;
        }

        T participant = this.createParticipant(player).get();
        if (participant == null) return false;

        Messages.get().getPlayerJoinedGame().send(this, "player", player.getName());
        Messages.get().getJoinedGame().send(player, "game", ArcadeUtils.niceify(this.identifier));
        this.participants.put(player.getUniqueId(), participant);
        return true;
    }

    /**
     * A request from the player to leave the game
     *
     * @param player     The player leaving the game
     * @param isRagequit Whether the player ragequit from the game
     * @return Whether the player successfully left
     */
    public boolean leave(Player player, boolean isRagequit) {
        // The location of the player
        if (this.location == null) {
            Messages.get().getUnknownPosition().send(player);
            return false;
        }

        // Check whether the player has already joined
        if (!this.participants.containsKey(player.getUniqueId())) {
            Messages.get().getNotJoined().send(player);
            return false;
        }

        // If the game is active, stop it instead
        if (this.active) {
            this.stop(true);
            return true;
        }

        Messages.get().getLeftGame().send(player);
        this.participants.remove(player.getUniqueId());
        if (isRagequit) Messages.get().getPlayerRageQuit().send(this, "player", player.getName());
        else Messages.get().getPlayerLeftGame().send(this, "player", player.getName());
        return true;
    }

    /**
     * Start the game for the participating players
     */
    public abstract void start();

    /**
     * Stop the game from continuing
     *
     * @param cancelled Whether the game was cancelled
     */
    public abstract void stop(boolean cancelled);

    /**
     * Unload the game from the world it's in
     */
    public abstract void unload();

    /**
     * Creates a new participant instance for the game
     *
     * @param target The player participant
     * @return The resulting participant
     */
    public abstract Supplier<T> createParticipant(Player target);

    /**
     * Gets the audience.
     *
     * @return the audience
     * @since 4.0.0
     */
    @Override
    public @NotNull Audience audience() {
        return Audience.audience(this.participants.values());
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<UUID, T> getParticipants() {
        return participants;
    }

    public double getWager() {
        return wager;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
