package dev.oribuin.arcade.games.chess.board;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.MoveCheckResult;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.ALLY;
import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.AVAILABLE_SPACE;
import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.ENEMY;
import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.IMMUNE;
import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.OUT_OF_BOUNDS;
import static dev.oribuin.arcade.games.chess.piece.PieceType.BISHOP;
import static dev.oribuin.arcade.games.chess.piece.PieceType.KING;
import static dev.oribuin.arcade.games.chess.piece.PieceType.KNIGHT;
import static dev.oribuin.arcade.games.chess.piece.PieceType.PAWN;
import static dev.oribuin.arcade.games.chess.piece.PieceType.QUEEN;
import static dev.oribuin.arcade.games.chess.piece.PieceType.ROOK;

/**
 * Handles all logic regarding the actual game of chess, Placing pieces and moving pieces
 */
public class ChessBoard {

    private static final char[] LETTERS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    private final Table<Integer, Integer, ChessPiece> pieces;

    /**
     * Creates a new chess board for the plugin 
     */
    public ChessBoard() {
        this.pieces = HashBasedTable.create();

        // Set the pawn pieces into the board
        for (int slot = 1; slot <= 8; slot++) {
            this.place(PAWN, PieceTeam.WHITE, new BoardPosition(2, slot));
            this.place(PAWN, PieceTeam.BLACK, new BoardPosition(7, slot));
        }

        // Set the rook pieces
        this.place(ROOK, PieceTeam.WHITE, new BoardPosition(1, 1));
        this.place(ROOK, PieceTeam.WHITE, new BoardPosition(1, 8));
        this.place(ROOK, PieceTeam.BLACK, new BoardPosition(8, 1));
        this.place(ROOK, PieceTeam.BLACK, new BoardPosition(8, 8));

        // Set the knight pieces
        this.place(KNIGHT, PieceTeam.WHITE, new BoardPosition(1, 2));
        this.place(KNIGHT, PieceTeam.WHITE, new BoardPosition(1, 7));
        this.place(KNIGHT, PieceTeam.BLACK, new BoardPosition(8, 2));
        this.place(KNIGHT, PieceTeam.BLACK, new BoardPosition(8, 7));

        // Set the bishop pieces 
        this.place(BISHOP, PieceTeam.WHITE, new BoardPosition(1, 3));
        this.place(BISHOP, PieceTeam.WHITE, new BoardPosition(1, 6));
        this.place(BISHOP, PieceTeam.BLACK, new BoardPosition(8, 3));
        this.place(BISHOP, PieceTeam.BLACK, new BoardPosition(8, 6));

        // Set the king & queens pieces 
        this.place(QUEEN, PieceTeam.WHITE, new BoardPosition(1, 4));
        this.place(QUEEN, PieceTeam.BLACK, new BoardPosition(8, 4));
        this.place(KING, PieceTeam.WHITE, new BoardPosition(1, 5));
        this.place(KING, PieceTeam.BLACK, new BoardPosition(8, 5));
    }

    /**
     * Create a new piece and place it on the board 
     *
     * @param type The type of piece to place
     * @param team The team the piece is on
     * @param pos Where the piece is locating
     */
    private void place(@NotNull PieceType type, @NotNull PieceTeam team, @NotNull BoardPosition pos) {
        this.pieces.put(pos.row(), pos.column(), type.createPiece(team, pos));
    }

    /**
     * Check whether a piece at a specified position is an enemy to an existing piece
     *
     * @param piece    The piece to check
     * @param position The position of the enemy
     * @return Whether the two teams are enemies
     */
    public boolean isEnemy(@NotNull ChessPiece piece, @NotNull BoardPosition position) {
        ChessPiece existing = this.getPiece(position);
        if (existing == null) return false;
        return piece.getTeam() != existing.getTeam();
    }

    /**
     * Check whether a piece is able to occupy a position on the board
     *
     * @param piece    The piece to check
     * @param position Where the piece wants to go
     * @return Whether the position is available
     */
    public MoveCheckResult checkPosition(@NotNull ChessPiece piece, @NotNull BoardPosition position) {
        if (!ChessBoard.isInBounds(position)) return OUT_OF_BOUNDS; // not in bounds so its not available

        ChessPiece existing = this.getPiece(position);
        if (existing == null) return AVAILABLE_SPACE;
        if (existing.getType() == KING) return IMMUNE;

        return piece.getTeam() == existing.getTeam() ? ALLY : ENEMY;
    }

    /**
     * Check whether a piece is able to occupy a position on the board
     *
     * @param piece    The piece to check
     * @param position Where the piece wants to go
     * @return Whether the position is available
     */
    public boolean isAvailable(@NotNull ChessPiece piece, @NotNull BoardPosition position) {
        MoveCheckResult result = this.checkPosition(piece, position);
        return result == AVAILABLE_SPACE || result == ENEMY;
    }

    /**
     * Move a chess piece onto the placement in the board
     *
     * @param piece    The piece to move
     * @param position The position to move it to
     */
    public boolean move(@NotNull ChessPiece piece, @NotNull BoardPosition position) {
        // Check whether the position is out of bounds
        if (!isInBounds(position)) return false;

        ChessPiece existing = this.getPiece(position);
        if (existing == null) {
            piece.move(position);
            return true;
        }

        // Check if the pieces are on the same team
        if (existing.getTeam() == piece.getTeam()) return false;

        // Check if the piece is a king because you cant take those 
        if (existing.getType() == KING) return false;

        existing.remove();
        piece.move(position);
        return true;
    }

    /**
     * Remove the existing piece from the board
     *
     * @param position The position to move
     */
    public void remove(@NotNull BoardPosition position) {
        ChessPiece existing = this.getPiece(position);
        if (existing != null) existing.remove();
    }

    /**
     * Get a piece that is on the board
     *
     * @param position The piece that is on the board
     * @return The existing piece if available
     */
    @Nullable
    public ChessPiece getPiece(@NotNull BoardPosition position) {
        if (!isInBounds(position)) return null;

        return this.pieces.get(position.row(), position.column());
    }

    /**
     * Get the character for the row
     *
     * @param row The row to get
     * @return The associated char
     */
    public static char getRowChar(int row) {
        return LETTERS[row - 1];
    }

    /**
     * Check whether a position is off the grid
     *
     * @param slot The slot to check
     * @return Whether the position is accepted
     */
    public static boolean isInBounds(int slot) {
        return slot <= 8 && slot >= 1;
    }

    /**
     * Check whether a position is off the grid
     *
     * @param slot The slot to check
     * @return Whether the position is accepted
     */
    public static boolean isInBounds(@NotNull BoardPosition slot) {
        return isInBounds(slot.row()) || isInBounds(slot.column());
    }

    public Table<Integer, Integer, ChessPiece> getPieces() {
        return pieces;
    }
}
