package dev.oribuin.arcade.manager;

import dev.oribuin.arcade.ArcadePlugin;

public interface Manager {

    /**
     * The task that runs when the plugin is loaded/reloaded
     *
     * @param plugin The plugin reloading
     */
    void reload(ArcadePlugin plugin);

    /**
     * The task that runs when the plugin is disabled, usually takes priority over {@link Manager#reload(ArcadePlugin)}
     *
     * @param plugin The plugin being disabled
     */
    void disable(ArcadePlugin plugin);

}
