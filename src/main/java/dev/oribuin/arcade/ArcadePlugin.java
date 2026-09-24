package dev.oribuin.arcade;

import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.event.GameRegistrationEvent;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.config.DatabaseSettings;
import dev.oribuin.arcade.config.Messages;
import dev.oribuin.arcade.config.loader.ConfigLoader;
import dev.oribuin.arcade.games.connectfour.ConnectGame;
import dev.oribuin.arcade.hook.PAPIProvider;
import dev.oribuin.arcade.listener.GameListener;
import dev.oribuin.arcade.manager.CommandManager;
import dev.oribuin.arcade.manager.DataManager;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import dev.oribuin.arcade.scheduler.wrapper.SchedulerWrapper;
import dev.oribuin.arcade.util.NMSUtil;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class ArcadePlugin extends JavaPlugin implements Listener {

    private static ArcadePlugin instance;
    private ConfigLoader configLoader;
    private CommandManager commandManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load the plugin configs
        this.configLoader = new ConfigLoader();
        this.configLoader.loadConfig(Messages.class, "messages");
        this.configLoader.loadConfig(DatabaseSettings.class, "database");

        new GameRegistry();

        // Register plugin listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new GameListener(this), this);

        // Register plugin hooks
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new PAPIProvider(this).register();
        }

        // Load the plugin managers
        this.commandManager = new CommandManager(this);
        this.dataManager = new DataManager(this);
        this.dataManager.reload(this);
    }

    /**
     * Register the games into the plugin
     *
     * @param event The event to register
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onRegister(GameRegistrationEvent event) {
        System.out.println("Register game event called");
        event.register("connect_four", ConnectGame::new);
    }

    @Override
    public void onDisable() {
        for (ArcadeGame<?> game : new ArrayList<>(GameRegistry.get().getInstances().values())) {
            if (NMSUtil.isFolia()) PluginScheduler.get().runTaskAtLocation(game.getLocation(), game::unload);
            else game.unload();
        }

        GameRegistry.get().getInstances().clear();
    }

    public static ArcadePlugin getInstance() {
        return instance;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
