package dev.oribuin.arcade.api.participant;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A basic implementation of a participant, No unique properties or methods
 *
 * @see Participant Base class for all participants
 */
public class Member extends Participant {


    /**
     * Create a new participant constructor
     *
     * @param uniqueId The UUID of the participant
     */
    public Member(@NotNull UUID uniqueId) {
        super(uniqueId);
    }

    /**
     * Create a new participant constructor
     *
     * @param player The participant player
     */
    public Member(@NotNull Player player) {
        super(player);
    }

}
