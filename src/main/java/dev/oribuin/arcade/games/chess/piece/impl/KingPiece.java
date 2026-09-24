package dev.oribuin.arcade.games.chess.piece.impl;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.MoveCheckResult;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.oribuin.arcade.games.chess.piece.MoveCheckResult.ENEMY;

/**
 * Represents a piece that will exist on the board for a player to move
 * <p>
 * This piece is literally the goat
 */
public class KingPiece extends ChessPiece {

    private static final @NotNull ItemStack WHITE = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThmMWM4YjJlNzU3ZDIwMjEzYTc4NmU3MmY3OTk2NDAyMTRkN2I5MGJlNDc2YjdhOWVhODYwN2RmY2EzODcifX19");
    private static final @NotNull ItemStack BLACK = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FhNWNhYWYxMTNiYTMzNWY4ODc1YmJiMDQxNmQ4N2U3NzA3OTMyYTZkY2M1NTdjY2YzODlmNWE3ZmEifX19");
    
    /**
     * Create a new chess piece to be placed on the board
     *
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public KingPiece(PieceTeam team, BoardPosition position) {
        super(PieceType.KING, team, position);
    }

    /**
     * Apply a function to the display entity on the board
     *
     * @return The function to apply
     */
    @Override
    public Consumer<ItemDisplay> apply() {
        return x -> x.setItemStack(team == PieceTeam.WHITE ? WHITE : BLACK);
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
        int distance = 1;

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
