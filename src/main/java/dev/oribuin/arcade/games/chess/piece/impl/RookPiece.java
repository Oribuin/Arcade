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
 * This piece can move vertically or horizontally at any distance but cannot move diagonally
 */
public class RookPiece extends ChessPiece {

    /**
     * Create a new chess piece to be placed on the board
     *
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public RookPiece(PieceTeam team, BoardPosition position) {
        super(PieceType.ROOK, team, position);
    }

    /**
     * Gets the available positions the piece can move to
     *
     * @param board The board to check against
     * @return The available positions
     */
    @Override
    public @NotNull List<BoardPosition> getAvailable(@NotNull ChessBoard board) {
        List<BoardPosition> result = new ArrayList<>();
        int distance = 8;

        // region Check left positions
        for (int i = distance; i <= distance; i++) {
            BoardPosition available = new BoardPosition(
                    this.position.row(),
                    this.position.column() - i
            );

            if (!ChessBoard.isInBounds(available)) break;
            ChessPiece existing = board.getPiece(available);
            result.add(available);
            if (existing != null) break;
        }
        // endregion
        // region Check right positions
        for (int i = distance; i <= distance; i++) {
            BoardPosition available = new BoardPosition(
                    this.position.row(),
                    this.position.column() + i
            );

            if (!ChessBoard.isInBounds(available)) break;
            ChessPiece existing = board.getPiece(available);
            result.add(available);
            if (existing != null) break;
        }
        // endregion
        // region Check down positions
        for (int i = distance; i <= distance; i++) {
            BoardPosition available = new BoardPosition(
                    this.position.row() - i,
                    this.position.column()
            );

            if (!ChessBoard.isInBounds(available)) break;
            ChessPiece existing = board.getPiece(available);
            result.add(available);
            if (existing != null) break;
        }
        // endregion
        // region Check up positions
        for (int i = distance; i <= distance; i++) {
            BoardPosition available = new BoardPosition(
                    this.position.row() + 1,
                    this.position.column()
            );

            if (!ChessBoard.isInBounds(available)) break;
            ChessPiece existing = board.getPiece(available);
            result.add(available);
            if (existing != null) break;
        }
        // endregion

        return result;
    }

    /**
     * Check on the board whether a slot is available for the rook
     *
     * @param board The board to check against
     * @param pos   The target position
     * @return whether the rook can move there
     */
    private boolean isAvailable(@NotNull ChessBoard board, @NotNull BoardPosition pos) {
        ChessPiece existing = board.getPiece(pos);
        if (existing == null) return true;

        return board.isEnemy(this, pos);
    }

}
