package dev.oribuin.arcade.games.chess.piece.impl;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.MoveCheckResult;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.i18nformatter.qual.I18nFormat;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a piece that will exist on the board for a player to move
 * <p>
 * This piece can only move forward once and only attack diagonally.
 */
public class PawnPiece extends ChessPiece {

    private static final @NotNull ItemStack WHITE = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWExNmZmNjBmZmZmMWI5MjJlZDE1NDdhY2E0NDFmYTNjMTQ4Y2M3ZTZjMzcyMTY2MWM5MGVhMWMxMDFmYTg0MSJ9fX0=");
    private static final @NotNull ItemStack BLACK = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGI2YjAxOTc4YTYzMTgzZmMyZDhhM2MzMTRlNjc3Njg5YWVkZTA4M2VhMzNjMzVkMjcxODdmOTcxODI5MTUifX19");

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
     * Apply a function to the display entity on the board
     *
     * @return The function to apply
     */
    @Override
    public Consumer<ItemDisplay> apply() {
        return x -> x.setItemStack(team == PieceTeam.WHITE ? WHITE : BLACK);
    }

    /**
     * Move the chess piece to the new position
     *
     * @param board The board the piece is on
     * @param position The new position of the piece
     */
    @Override
    public void move(@NotNull ChessBoard board, @NotNull BoardPosition position) {
        super.move(board, position);
        this.hasMoved = true; // TODO: Fix this literally not working at all
    }
    
    /**
     * Gets the available positions the piece can move to
     *
     * @param board The board to check against
     * @return The available positions
     */
    @Override
    public @NotNull List<BoardPosition> getAvailable(@NotNull ChessBoard board) {
        int moveDistance = team == PieceTeam.WHITE ? 1 : -1;

        List<BoardPosition> result = new ArrayList<>();
        // region Check whether the pawn can move forward
        BoardPosition ahead = new BoardPosition(
                this.position.getRow() + moveDistance, 
                this.position.getColumn()
        );
        
        MoveCheckResult check = board.checkPosition(this, ahead);
        if (check == MoveCheckResult.AVAILABLE_SPACE) result.add(ahead);
        
        // Pawns have the ability to move twice on their first turn
        if (!this.hasMoved && result.contains(ahead)) {
            BoardPosition aheadAgain = new BoardPosition(
                    this.position.getRow() + (team == PieceTeam.WHITE ? 2 : -2),
                    this.position.getColumn()
            );

            MoveCheckResult checkAgain = board.checkPosition(this, aheadAgain);
            if (checkAgain == MoveCheckResult.AVAILABLE_SPACE) result.add(aheadAgain);
        }

        // region Check whether the pawn can attack diagonally
        BoardPosition leftPos = new BoardPosition(
                this.position.getRow() - 1,
                this.position.getColumn() + moveDistance
        );

        BoardPosition rightPos = new BoardPosition(
                this.position.getRow() + 1,
                this.position.getColumn() + moveDistance
        );

        if (board.checkPosition(this, leftPos) == MoveCheckResult.ENEMY) result.add(leftPos);
        if (board.checkPosition(this, rightPos) == MoveCheckResult.ENEMY) result.add(rightPos);
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
