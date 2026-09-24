package dev.oribuin.arcade.games.chess.piece.impl;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a piece that will exist on the board for a player to move
 * <p>
 * This piece is weird and moves forward/backwards/sideways twice and left and right once
 */
public class KnightPiece extends ChessPiece {

    private static final @NotNull ItemStack WHITE = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ5ZDU3YWY1MGU2NDY4NjI1OTU2MmNlZTU2YjRjOWE4YjhkMzVhNzhjNTFjZTA2NDZmODk3Mzg4NDY2NSJ9fX0=");
    private static final @NotNull ItemStack BLACK = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzUyNDJmNGNiMDE3ZWRjNDc4ODk3ZTVkMGY1YjhmMmE2ZjVlZWNjZmM4NjVmNGZmNzIyNzZiZmI5ZDIyMWQ1In19fQ==");
    
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
        int row = 2;
        int column = 1;

        // region Forward Left & Right
        BoardPosition forwardLeft = new BoardPosition(
                this.position.row() - column,
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
                this.position.row() - column,
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
