package dev.oribuin.arcade.games.chess.participant;

import dev.oribuin.arcade.api.participant.Participant;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChessMaster extends Participant {
    
    private final PieceTeam team;
    
    /**
     * Create a new participant constructor
     *
     * @param uniqueId The UUID of the participant
     */
    public ChessMaster(@NotNull UUID uniqueId, PieceTeam team) {
        super(uniqueId);
        this.team = team;
    }

    /**
     * Create a new participant constructor
     *
     * @param player The participant player
     */
    public ChessMaster(@NotNull Player player, PieceTeam team) {
        super(player);
        this.team = team;
    }

    public PieceTeam getTeam() {
        return team;
    }
}
