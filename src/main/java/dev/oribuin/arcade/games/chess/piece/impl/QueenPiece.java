package dev.oribuin.arcade.games.chess.piece.impl;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.MoveCheckResult;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.ENEMY;

/**
 * Represents a piece that will exist on the board for a player to move
 * <p>
 * This piece is literally the goat
 */
public class QueenPiece extends ChessPiece {

    /**
     * Create a new chess piece to be placed on the board
     *
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public QueenPiece(PieceTeam team, BoardPosition position) {
        super(PieceType.QUEEN, team, position);
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

        // up & down
        for (int i = -distance; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(this.position.row(), this.position.column() + i);
            if (pos.row() == this.position.row() && pos.column() == this.position.column()) continue;

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (!checkResult.isTakeable()) break; // Check whether the position is takeable

            // Add the position and cancel search if enemy found
            result.add(pos);
            if (checkResult == ENEMY) break;
        }


        // left & right
        for (int i = -distance; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(this.position.row() + i, this.position.column());
            if (pos.row() == this.position.row() && pos.column() == this.position.column()) continue;

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (!checkResult.isTakeable()) break; // Check whether the position is takeable

            // Add the position and cancel search if enemy found
            result.add(pos);
            if (checkResult == ENEMY) break;
        }

        // region Top Right
        for (int i = 1; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(
                    this.position.row() + i,
                    this.position.column() + i
            );

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (!checkResult.isTakeable()) break; // Check whether the position is takeable

            // Add the position and cancel search if enemy found
            result.add(pos);
            if (checkResult == ENEMY) break;
        }
        // endregion        

        // region Top Left
        for (int i = 1; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(
                    this.position.row() - i,
                    this.position.column() + i
            );

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (!checkResult.isTakeable()) break; // Check whether the position is takeable

            // Add the position and cancel search if enemy found
            result.add(pos);
            if (checkResult == ENEMY) break;
        }
        // endregion

        // region Bottom Right
        for (int i = 1; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(
                    this.position.row() + i,
                    this.position.column() - i
            );

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (!checkResult.isTakeable()) break; // Check whether the position is takeable

            // Add the position and cancel search if enemy found
            result.add(pos);
            if (checkResult == ENEMY) break;
        }
        // endregion        

        // region Bottom Left
        for (int i = 1; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(
                    this.position.row() - i,
                    this.position.column() - i
            );

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (!checkResult.isTakeable()) break; // Check whether the position is takeable

            // Add the position and cancel search if enemy found
            result.add(pos);
            if (checkResult == ENEMY) break;
        }
        // endregion

        return result;
    }

}
