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
}
