package dev.oribuin.arcade.manager;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.command.AdminCommand;
import dev.oribuin.arcade.config.Messages;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.setting.Configurable;
import org.incendo.cloud.setting.ManagerSetting;
import org.jetbrains.annotations.NotNull;

import javax.naming.NoPermissionException;
import java.util.function.Supplier;

public class CommandManager extends LegacyPaperCommandManager<CommandSender> implements Manager {

    private final ArcadePlugin plugin;
    private final AnnotationParser<CommandSender> parser;

    public CommandManager(@NotNull ArcadePlugin owningPlugin) {
        super(
                owningPlugin,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );

        this.plugin = owningPlugin;
        // Register the command manager
        Configurable<ManagerSetting> commandSettings = this.settings();
        commandSettings.set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        commandSettings.set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        // Register argument parser
        this.parser = new AnnotationParser<>(this, CommandSender.class);

        // Register capabilities
//        if (this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
//            this.registerBrigadier();
        if (this.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.registerAsynchronousCompletions();
        }


        this.exceptionController()
                .registerHandler(NoPermissionException.class, x -> Messages.get()
                        .getNoPermission().send(x.context().sender())
                )
                .registerHandler(InvalidSyntaxException.class, x -> Messages.get()
                        .getInvalidSyntax().send(x.context().sender(), "syntax", x.exception().correctSyntax())
                )
                .registerHandler(InvalidCommandSenderException.class, x -> Messages.get()
                        .getRequirePlayer().send(x.context().sender(), "sender", x.context().sender().getName())
                );

        // Register additional stuff down here :3
        // Register all the plugin commands
        try {
            this.parser.parse(new AdminCommand(this.plugin));
        } catch (IllegalArgumentException ex) {
            owningPlugin.getLogger().severe("There was an issue parsing a command: " + ex.getMessage());
        }
    }

    /**
     * The task that runs when the plugin is loaded/reloaded
     *
     * @param plugin The plugin reloading
     */
    @Override
    public void reload(ArcadePlugin plugin) {
    }

    /**
     * The task that runs when the plugin is disabled, usually takes priority over {@link Manager#reload(FishingPlugin)}
     *
     * @param plugin The plugin being disabled
     */
    @Override
    public void disable(ArcadePlugin plugin) {
    }

    /**
     * Register a command parser into the plugin command manager
     *
     * @param type   The type class being parsed
     * @param parser The argument parser
     * @param <T>    The type being parsed
     */
    public <T> void registerParser(Class<T> type, Supplier<ArgumentParser<CommandSender, T>> parser) {
        ParserDescriptor<CommandSender, T> descriptor = ParserDescriptor.of(parser.get(), type);
        this.parserRegistry().registerParser(descriptor);
    }

    public ArcadePlugin getPlugin() {
        return plugin;
    }

    public AnnotationParser<CommandSender> getParser() {
        return parser;
    }
}
