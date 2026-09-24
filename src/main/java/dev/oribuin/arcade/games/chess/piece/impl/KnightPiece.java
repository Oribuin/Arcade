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
 * This piece is weird and moves forward/backwards/sideways twice and left and right once
 */
public class KnightPiece extends ChessPiece {

    /**
     * Create a new chess piece to be placed on the board
     *
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public KnightPiece(PieceTeam team, BoardPosition position) {
        super(PieceType.KNIGHT, team, position);
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
        int row = 2;
        int column = 1;

        // region Forward Left & Right
        BoardPosition forwardLeft = new BoardPosition(
                this.position.row() + -column,
                this.position.column() + row
        );

        BoardPosition forwardRight = new BoardPosition(
                this.position.row() + column,
                this.position.column() + row
        );

        if (board.isAvailable(this, forwardLeft)) result.add(forwardLeft);
        if (board.isAvailable(this, forwardRight)) result.add(forwardRight);
        // endregion

        // region Backward Left & Right
        BoardPosition backwardLeft = new BoardPosition(
                this.position.row() + -column,
                this.position.column() - row
        );

        BoardPosition backwardRight = new BoardPosition(
                this.position.row() + column,
                this.position.column() - row
        );

        if (board.isAvailable(this, backwardLeft)) result.add(backwardLeft);
        if (board.isAvailable(this, backwardRight)) result.add(backwardRight);
        // endregion        

        // region Left Up & Down
        BoardPosition leftUp = new BoardPosition(
                this.position.row() - row,
                this.position.column() + column
        );

        BoardPosition leftDown = new BoardPosition(
                this.position.row() - row,
                this.position.column() - column
        );

        if (board.isAvailable(this, leftUp)) result.add(leftUp);
        if (board.isAvailable(this, leftDown)) result.add(leftDown);
        // endregion        
        
        // region Right Up & Down
        BoardPosition rightUp = new BoardPosition(
                this.position.row() + row,
                this.position.column() + column
        );

        BoardPosition rightDown = new BoardPosition(
                this.position.row() + row,
                this.position.column() - column
        );

        if (board.isAvailable(this, rightUp)) result.add(rightUp);
        if (board.isAvailable(this, rightDown)) result.add(rightDown);
        // endregion
        
        return result;
    }


}
