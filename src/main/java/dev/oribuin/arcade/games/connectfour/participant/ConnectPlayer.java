package dev.oribuin.arcade.games.connectfour.participant;

import dev.oribuin.arcade.api.participant.Participant;
import dev.oribuin.arcade.games.connectfour.token.TokenColour;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConnectPlayer extends Participant {

    private TokenColour colour;

    /**
     * Create a new participant constructor
     *
     * @param uniqueId The UUID of the participant
     */
    public ConnectPlayer(@NotNull UUID uniqueId, TokenColour colour) {
        super(uniqueId);
        this.colour = colour;
    }

    /**
     * Create a new participant constructor
     *
     * @param player The participant player
     */
    public ConnectPlayer(@NotNull Player player, TokenColour colour) {
        super(player);
        this.colour = colour;
    }

    public TokenColour getTokenColour() {
        return colour;
    }

    public void setColour(TokenColour colour) {
        this.colour = colour;
    }

}
