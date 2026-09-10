package dev.oribuin.arcade.api.game;

import dev.oribuin.arcade.api.participant.Participant;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ArcadeGame<T extends Participant> {

    protected final String identifier; // The identifier for the game 
    protected final Map<UUID, T> participants; // The people playing the game
    protected final double wager; // The money placed within the game 
    protected final int playerCount;  // The amount of people required to play the game
    protected Location location; // The centre location of the game
    private boolean active;

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

    public abstract void start();
    
    public abstract void unload();
    
    public abstract void remove();
    
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
