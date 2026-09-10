package dev.oribuin.arcade.api.participant;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * The participant class for a minigame, used to store information about a player in a match
 *
 * @see Member A basic implementation for a participant
 */
public abstract class Participant implements ForwardingAudience.Single {

    private UUID uniqueId; // dev.oribuin.arcade.api.participant.Participant's UUID
    private String name; // dev.oribuin.arcade.api.participant.Participant's name
    private Player player;

    /**
     * Create a new participant constructor
     *
     * @param uniqueId The UUID of the participant
     */
    public Participant(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.player = Bukkit.getPlayer(uniqueId);
        this.name = player != null ? player.getName() : null;
    }

    /**
     * Create a new participant constructor
     *
     * @param player The participant player
     */
    public Participant(@NotNull Player player) {
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
        this.player = player;
    }

    /**
     * Establish all the placeholders for the participant to use
     *
     * @return The placeholders
     */
    public Map<String, Object> placeholders() {
        return Map.of("name", this.name);
    }

    /**
     * Get the participant as a player
     *
     * @return The participant as a player
     */
    public Player getPlayer() {
        if (this.player != null && player.isOnline()) return this.player;

        Player player = Bukkit.getPlayer(this.uniqueId);
        if (player != null) {
            this.player = player;
            this.name = player.getName();
        }
        return player;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public @NotNull Audience audience() {
        if (this.player != null && !this.player.isOnline()) {
            this.player = null; // remove the player from the cache
            return Audience.empty();
        }

        if (this.player != null) return Audience.audience(this.player);

        this.player = Bukkit.getPlayer(this.uniqueId);
        return this.player != null ? Audience.audience(this.player) : Audience.empty();
    }

}
