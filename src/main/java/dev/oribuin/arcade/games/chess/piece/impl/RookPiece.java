package dev.oribuin.arcade.games.chess.piece.impl;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.MoveCheckResult;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.games.chess.piece.PieceType;
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
 * This piece can move vertically or horizontally at any distance but cannot move diagonally
 */
public class RookPiece extends ChessPiece {

    private static final @NotNull ItemStack WHITE = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBlZDQ4MWQxYjhkODNmNDcyNzMxZTNmYzNjOWI5NTFhMWVjNWU5ZDUzOTU1NDNkNThlMWRmNWEyZTNmM2QifX19");
    private static final @NotNull ItemStack BLACK = createSkullData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmJkOTU0YjM2Y2JjY2I0YzU4NzM2YjEzY2Q5MWM4ODM2NzhmNDYxNjQ0ZDBmYzJkNWZiMmYzMThiZmJmN2NkIn19fQ==");

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
        int distance = 8;

        // check the up position
        for (int i = 0; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(this.position.getRow(), this.position.getColumn() + i);
            if (pos.equals(this.position)) continue;

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (checkResult == MoveCheckResult.OUT_OF_BOUNDS) continue; // out of bounds = continue

            if (checkResult == MoveCheckResult.AVAILABLE_SPACE) { // available slot, keep going
                result.add(pos);
                continue;
            }

            // Add the enemy's position to the list of results and then stop searching 
            if (checkResult == ENEMY) {
                result.add(pos);
                break;
            }

            break; // immune or an ally
        }
        // check the down position

        // region check the down position
        for (int i = 0; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(this.position.getRow(), this.position.getColumn() - i);
            if (pos.equals(this.position)) continue;

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (checkResult == MoveCheckResult.OUT_OF_BOUNDS) continue; // out of bounds = continue

            if (checkResult == MoveCheckResult.AVAILABLE_SPACE) { // available slot, keep going
                result.add(pos);
                continue;
            }

            // Add the enemy's position to the list of results and then stop searching 
            if (checkResult == ENEMY) {
                result.add(pos);
                break;
            }

            break; // immune or an ally
        }
        // endregion
        
        // region Check the right position 
        for (int i = 0; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(this.position.getRow() + i, this.position.getColumn());
            if (pos.equals(this.position)) continue;

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (checkResult == MoveCheckResult.OUT_OF_BOUNDS) continue; // out of bounds = continue
            
            if (checkResult == MoveCheckResult.AVAILABLE_SPACE) { // available slot, keep going
                result.add(pos);
                continue;
            }
            
            // Add the enemy's position to the list of results and then stop searching 
            if (checkResult == ENEMY) {
                result.add(pos);
                break;
            }
            
            break; // immune or an ally
        }
        // endregion

        // region Check the left position 
        for (int i = 0; i <= distance; i++) {
            BoardPosition pos = new BoardPosition(this.position.getRow() - i, this.position.getColumn());
            if (pos.equals(this.position)) continue;

            MoveCheckResult checkResult = board.checkPosition(this, pos);
            if (checkResult == MoveCheckResult.OUT_OF_BOUNDS) continue; // out of bounds = continue

            if (checkResult == MoveCheckResult.AVAILABLE_SPACE) { // available slot, keep going
                result.add(pos);
                continue;
            }

            // Add the enemy's position to the list of results and then stop searching 
            if (checkResult == ENEMY) {
                result.add(pos);
                break;
            }

            break; // immune or an ally
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
