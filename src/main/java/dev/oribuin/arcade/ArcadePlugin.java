package dev.oribuin.arcade;

import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.config.Messages;
import dev.oribuin.arcade.config.loader.ConfigLoader;
import dev.oribuin.arcade.games.connectfour.ConnectGame;
import dev.oribuin.arcade.listener.GameListener;
import dev.oribuin.arcade.manager.CommandManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

import static dev.oribuin.arcade.api.GameRegistry.GAME_INSTANCES;

public class ArcadePlugin extends JavaPlugin {

    private static ArcadePlugin instance;
    private ConfigLoader configLoader;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load the plugin configs
        this.configLoader = new ConfigLoader();
        this.configLoader.loadConfig(Messages.class);

        // Load the plugin managers
        this.commandManager = new CommandManager(this);

        // Register plugin listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new GameListener(this), this);

        // Register the games into the plugin
        // TODO: Make this an event or change how its done
        GameRegistry.register("connect_four", ConnectGame::new);
    }

    @Override
    public void onDisable() {
        for (ArcadeGame<?> game : new ArrayList<>(GAME_INSTANCES.values())) {
            game.unload();
        }

        GAME_INSTANCES.clear();
    }

    public static ArcadePlugin getInstance() {
        return instance;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
