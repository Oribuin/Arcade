package dev.oribuin.arcade.games.chess.piece.impl;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a piece that will exist on the board for a player to move
 * <p>
 * This piece can only move forward once and only attack diagonally.
 */
public class PawnPiece extends ChessPiece {

    private boolean hasMoved;

    /**
     * Create a new chess piece to be placed on the board
     *
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public PawnPiece(PieceTeam team, BoardPosition position) {
        super(PieceType.PAWN, team, position);
        this.hasMoved = false;
    }

    /**
     * Move the chess piece to the new position
     *
     * @param position The new position of the piece
     */
    @Override
    public void move(@NotNull BoardPosition position) {
        super.move(position);
        this.hasMoved = true;
    }

    /**
     * Gets the available positions the piece can move to
     *
     * @param board The board to check against
     * @return The available positions
     */
    @Override
    public @NotNull List<BoardPosition> getAvailable(@NotNull ChessBoard board) {
        int baseDistance = this.hasMoved ? 1 : 2;
        int moveDistance = team == PieceTeam.WHITE ? baseDistance : -baseDistance;

        List<BoardPosition> result = new ArrayList<>();
        // region Check whether the pawn can move forward 
        for (int i = 1; i <= moveDistance; i++) {
            BoardPosition available = new BoardPosition(
                    this.position.row(),
                    this.position.column() + i
            );

            ChessPiece existing = board.getPiece(available);
            if (existing != null) break; // Pawns are not able to hop over a piece

            result.add(available);
        }
        // endregion 
        moveDistance = team == PieceTeam.WHITE ? 1 : -1;

        // region Check whether the pawn can attack diagonally
        BoardPosition leftPos = new BoardPosition(
                this.position.row() - 1,
                this.position.column() + moveDistance
        );
        
        BoardPosition rightPos = new BoardPosition(
                this.position.row() + 1,
                this.position.column() + moveDistance
        );
        
        if (board.isAvailable(this, leftPos)) result.add(leftPos);
        if (board.isAvailable(this, rightPos)) result.add(rightPos);
        // endregion
 
        return result;
    }

    /**
     * Check if a pawn has moved on the board yet
     *
     * @return Whether the pawn has moved
     */
    public boolean isHasMoved() {
        return hasMoved;
    }

}
