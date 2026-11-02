package dev.oribuin.arcade.config;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.config.type.TextMessage;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Messages {

    public static final String PREFIX = "<#94bc80><b>Arcade</b> <gray>| <white>";

    @Comment("The message sent when a user reloads the plugin")
    private TextMessage reload = new TextMessage(PREFIX + "You have reloaded the plugin in <#93bc80><time><white>ms");

    @Comment("The message sent when a player does not have permission to do something.")
    private TextMessage noPermission = new TextMessage(PREFIX + "You do not have permission to do this");

    @Comment("The message sent when a player does not have permission to do something.")
    private TextMessage requirePlayer = new TextMessage(PREFIX + "You need to be sender type of <#94bc80><sender><white> to run this command");

    @Comment("The message sent when a player gets the syntax for a message wrong")
    private TextMessage invalidSyntax = new TextMessage(PREFIX + "You have provided invalid syntax. The correct usage is: <#94bc80><syntax>");

    @Comment("The message sent when a player tries to join an active game")
    private TextMessage alreadyActive = new TextMessage(PREFIX + "This game is already running");

    @Comment("The message sent when a player tries to join a game that isnt in the world")
    private TextMessage unknownPosition = new TextMessage(PREFIX + "How are you joining a minigame with no location?");

    @Comment("The message sent when a player tries to join a game they're already involved in")
    private TextMessage alreadyJoined = new TextMessage(PREFIX + "You have already joined this game");

    @Comment("The message sent when a player tries to leave a game they're not involved in")
    private TextMessage notJoined = new TextMessage(PREFIX + "You are not part of this game");

    @Comment("The message sent when a player joins a game")
    private TextMessage joinedGame = new TextMessage(PREFIX + "You have joined a game of <#93bc80><game>");

    @Comment("The message sent to the other game players when a player joins a game")
    private TextMessage playerJoinedGame = new TextMessage(PREFIX + "<#93bc80><player> <white>has joined your game");

    @Comment("The message sent when a player leaves a game")
    private TextMessage leftGame = new TextMessage(PREFIX + "You have left the arcade game");

    @Comment("The message sent to the other game players when a player leaves a game")
    private TextMessage playerLeftGame = new TextMessage(PREFIX + "<#93bc80><player> <white>has left the game");

    @Comment("The message sent when a player ragequits a game")
    private TextMessage playerRageQuit = new TextMessage(PREFIX + "<#93bc80><player> <white>has ragequit from the match");

    @Comment("The message sent when a player does something while it's not their turn")
    private TextMessage notUsersTurn = new TextMessage(PREFIX + "Please wait for your turn before you do this");

    @Comment("The message sent when a player has won")
    private TextMessage playerWon = new TextMessage(PREFIX + "<#93bc80><winner> <white>has won against <#93bc80><losers>")
            .titleHeader("<#93bc80><b>Victory Royale")
            .titleSubtitle("<white><winner> has won <game>");

    @Comment("The joining message between each player on the info board")
    private String activeInfoJoiner = " <gray>vs ";

    @Comment("The starting message on the info board")
    private String activeInfoStart = "<#93bc80>";

    private String inactiveInfoBoard = "<#93bc80>Waiting for <white><remaining> <#93bc80>players";

    @Comment("The messages sent for connect four")
    private ConnectFour connectFour = new ConnectFour();
    
    @Comment("The messages sent for chess")
    private Chess chess = new Chess();

    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class ConnectFour {

        @Comment("The message sent when a player tries to place a token on a row that is full")
        private TextMessage notUsersTurn = new TextMessage(PREFIX + "You cannot place a token on this row");

        public TextMessage getNotUsersTurn() {
            return notUsersTurn;
        }
    }
    
    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class Chess {
        public static final String CHESS_PREFIX = "<#94bc80><b>Chess</b> <gray>| <white>";

        @Comment("The info board text for the current team")
        private String currentTeamInfo ="<white>It is <#93bc80><team><white> Turn <dark_gray>| <#93bc80><time>";
        
        private TextMessage cannotUse = new TextMessage(CHESS_PREFIX + "You cannot move a piece that doesn't belong to you");
        private TextMessage selectedPiece = new TextMessage(CHESS_PREFIX + "You have selected <#93bc80><piece> <position>");
        private TextMessage deselectedPiece = new TextMessage(CHESS_PREFIX + "You have deselected a piece");
        private TextMessage movedPiece = new TextMessage(CHESS_PREFIX + "<#93bc80><player><white> has moved <white><#93bc80><piece> <white>from <#93bc80><current> <white>➡ <#93bc80><target>");
        private TextMessage tookPiece = new TextMessage(CHESS_PREFIX + "<#93bc80><player> <white>took the opposing team's <#93bc80><piece>");
        private TextMessage cannotMoveHere  = new TextMessage(CHESS_PREFIX + "You cannot move this piece to this location");
        private TextMessage noAvailableMoves = new TextMessage(CHESS_PREFIX + "This piece has no available moves");
        
        public String getCurrentTeamInfo() {
            return currentTeamInfo;
        }

        public TextMessage getSelectedPiece() {
            return selectedPiece;
        }

        public TextMessage getDeselectedPiece() {
            return deselectedPiece;
        }

        public TextMessage getMovedPiece() {
            return movedPiece;
        }

        public TextMessage getTookPiece() {
            return tookPiece;
        }

        public TextMessage getCannotMoveHere() {
            return cannotMoveHere;
        }

        public TextMessage getNoAvailableMoves() {
            return noAvailableMoves;
        }

        public TextMessage getCannotUse() {
            return cannotUse;
        }
    }
    

    public static Messages get() {
        return ArcadePlugin.getInstance().getConfigLoader().get(Messages.class);
    }

    public TextMessage getReload() {
        return reload;
    }

    public TextMessage getNoPermission() {
        return noPermission;
    }

    public TextMessage getRequirePlayer() {
        return requirePlayer;
    }

    public TextMessage getInvalidSyntax() {
        return invalidSyntax;
    }

    public TextMessage getAlreadyActive() {
        return alreadyActive;
    }

    public TextMessage getUnknownPosition() {
        return unknownPosition;
    }

    public TextMessage getAlreadyJoined() {
        return alreadyJoined;
    }

    public TextMessage getNotJoined() {
        return notJoined;
    }

    public TextMessage getJoinedGame() {
        return joinedGame;
    }

    public TextMessage getPlayerJoinedGame() {
        return playerJoinedGame;
    }

    public TextMessage getLeftGame() {
        return leftGame;
    }

    public TextMessage getPlayerLeftGame() {
        return playerLeftGame;
    }

    public TextMessage getPlayerRageQuit() {
        return playerRageQuit;
    }

    public TextMessage getNotUsersTurn() {
        return notUsersTurn;
    }

    public TextMessage getPlayerWon() {
        return playerWon;
    }

    public String getActiveInfoJoiner() {
        return activeInfoJoiner;
    }

    public String getActiveInfoStart() {
        return activeInfoStart;
    }

    public String getInactiveInfoBoard() {
        return inactiveInfoBoard;
    }

    public ConnectFour getConnectFour() {
        return connectFour;
    }

    public Chess getChess() {
        return chess;
    }
}
