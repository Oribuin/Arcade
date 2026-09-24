package dev.oribuin.arcade.games.chess.piece;

import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.piece.impl.BishopPiece;
import dev.oribuin.arcade.games.chess.piece.impl.KingPiece;
import dev.oribuin.arcade.games.chess.piece.impl.KnightPiece;
import dev.oribuin.arcade.games.chess.piece.impl.PawnPiece;
import dev.oribuin.arcade.games.chess.piece.impl.QueenPiece;
import dev.oribuin.arcade.games.chess.piece.impl.RookPiece;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum PieceType {
    KING(KingPiece::new),
    QUEEN(QueenPiece::new),
    BISHOP(BishopPiece::new),
    KNIGHT(KnightPiece::new),
    ROOK(RookPiece::new),
    PAWN(PawnPiece::new);

    private final BiFunction<PieceTeam, BoardPosition, ChessPiece> supplier;

    PieceType(BiFunction<PieceTeam, BoardPosition, ChessPiece> supplier) {
        this.supplier = supplier;
    }

    /**
     * Create a new piece for a team 
     * @param team The team the piece is on
     * @param position The position of the board
     * @return A new chess piece
     */
    @NotNull
    public ChessPiece createPiece(@NotNull PieceTeam team, @NotNull BoardPosition position) {
        return this.supplier.apply(team, position);
    }

}
