package dev.oribuin.arcade.games.chess.board;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Get the position of the board
 *
 */
public final class BoardPosition {
    
    private final int row;
    private final int column;
    private Location location;
    private UUID square;
    
    /**
     * @param row    The row the position is on, this is typically somewhere from a-h
     * @param column The row the column is on, this is typically 1-8
     */
    public BoardPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    /**
     * Check whether a position is occupied already on the board
     *
     * @param board The board to check against
     * @return Whether this piece is occupied
     */
    public boolean isOccupied(@NotNull ChessBoard board) {
        return board.getPiece(this) != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BoardPosition position = (BoardPosition) o;
        return row == position.row && column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    /**
     * Get the board position as it's usually referred
     *
     * @return The board position
     */
    @Override
    public @NonNull String toString() {
        return ChessBoard.getRowChar(row) + "," + column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getSquare() {
        return square;
    }

    public void setSquare(UUID square) {
        this.square = square;
    }
}
