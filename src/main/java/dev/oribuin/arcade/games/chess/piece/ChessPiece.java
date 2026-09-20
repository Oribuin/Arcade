package dev.oribuin.arcade.games.chess.piece;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a piece that will exist on the board for a player to move
 */
public abstract class ChessPiece {

    protected final PieceType type;
    protected final PieceTeam team;
    protected BoardPosition position;

    /**
     * Create a new chess piece to be placed on the board
     *
     * @param type     The type of chess piece
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public ChessPiece(PieceType type, PieceTeam team, BoardPosition position) {
        this.type = type;
        this.team = team;
        this.position = position;
    }

    /**
     * Move the chess piece to the new position
     *
     * @param position The new position of the piece
     */
    public void move(@NotNull BoardPosition position) {
        this.position = position;
        // TODO: move the entity
    }

    /**
     * Remove a chess piece from the board
     */
    public void remove() {
        this.position = null;
        // TODO: Remove the entity
    }

    /**
     * Gets the available positions the piece can move to
     *
     * @param board The board to check against
     * @return The available positions
     */
    @NotNull
    public abstract List<BoardPosition> getAvailable(@NotNull ChessBoard board);

    public PieceType getType() {
        return type;
    }

    public PieceTeam getTeam() {
        return team;
    }

    public BoardPosition getPosition() {
        return position;
    }

    public void setPosition(BoardPosition position) {
        this.position = position;
    }
}
