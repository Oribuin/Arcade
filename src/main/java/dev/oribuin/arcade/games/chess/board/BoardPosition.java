package dev.oribuin.arcade.games.chess.board;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Get the position of the board
 *
 * @param row    The row the position is on, this is typically somewhere from a-h
 * @param column The row the column is on, this is typically 1-8
 */
public record BoardPosition(int row, int column) {
    
    /**
     * Check whether a position is occupied already on the board
     *
     * @param board The board to check against
     * @return Whether this piece is occupied
     */
    public boolean isOccupied(@NotNull ChessBoard board) {
        return board.getPiece(this) != null;
    }

    /**
     * Get the board position as it's usually referred
     *
     * @return The board position
     */
    @Override
    public @NonNull String toString() {
        return String.valueOf(ChessBoard.getRowChar(row) + column);
    }

}
