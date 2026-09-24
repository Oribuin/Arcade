package dev.oribuin.arcade.command;

import com.google.common.collect.Table;
import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TestCommand {

    private final ArcadePlugin plugin;


    public TestCommand(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Places an arcade game with a given name into the world
     *
     * @param sender The command sender
     * @param game   The game to place
     */
    @Command("arcade admin test chess")
    @Permission("arcade.admin")
    @CommandDescription("Places connect four down into the world")
    public void place(@NotNull CommandSender sender) {
        ChessBoard board = new ChessBoard();

        Table<Integer, Integer, ChessPiece> pieces = board.getPieces();
        int minimumRow = Collections.min(pieces.rowKeySet());
        int maximumRow = Collections.max(pieces.rowKeySet()) + 1;

        int minimumCol = Collections.min(pieces.columnKeySet());
        int maximumCol = Collections.max(pieces.columnKeySet()) + 1;

        int maximum = (maximumCol * 2) + 12;
        
        for (int r = minimumRow; r < maximumRow; r++) {
            StringBuilder line = new StringBuilder();
            sender.sendMessage("-".repeat(maximum));
            line.append("| ");
            for (int c = minimumCol; c < maximumCol; c++) {
                ChessPiece piece = pieces.get(r, c);
                char character = getPiece(piece);
                line.append(character).append(" | ");
            }
            sender.sendMessage(line.toString());
        }
        sender.sendMessage("-".repeat(maximum));
    }

    private static char getPiece(ChessPiece piece) {
        if (piece == null) return 'X';
        return switch (piece.getType()) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case BISHOP -> 'B';
            case KNIGHT -> 'H';
            case ROOK -> 'R';
            case PAWN -> 'P';
        };
    }

}
