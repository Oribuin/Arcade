package dev.oribuin.arcade.games.chess.participant;

import dev.oribuin.arcade.api.participant.Participant;
import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ChessPlayer extends Participant {

    private final PieceTeam team;
    private BoardPosition selected;

    /**
     * Create a new participant constructor
     *
     * @param uniqueId The UUID of the participant
     */
    public ChessPlayer(@NotNull UUID uniqueId, PieceTeam team) {
        super(uniqueId);
        this.team = team;
    }

    /**
     * Create a new participant con structor
     *
     * @param player The participant player
     */
    public ChessPlayer(@NotNull Player player, PieceTeam team) {
        super(player);
        this.team = team;
    }

    /**
     * Check whether the chess player can interact/use a piece
     * @param piece The piece to check
     * @return Whether the user can use that piece
     */
    public boolean canInteract(@Nullable ChessPiece piece) {
        if (piece == null) return false; // Piece does not exist; cannot interact
        return piece.getTeam() == this.team; // Piece belongs to the other team
    }

    @Override
    public String toString() {
        return "ChessPlayer{" +
                "team=" + team +
                ", selected=" + selected +
                '}';
    }

    public PieceTeam getTeam() {
        return team;
    }

    public BoardPosition getSelected() {
        return selected;
    }

    public void setSelected(BoardPosition selected) {
        this.selected = selected;
    }
}
