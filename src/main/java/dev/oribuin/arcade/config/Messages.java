package dev.oribuin.arcade.config;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.config.type.TextMessage;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
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
}
