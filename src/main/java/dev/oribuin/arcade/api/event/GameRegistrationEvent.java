package dev.oribuin.arcade.api.event;

import dev.oribuin.arcade.api.GameRegistry;
import dev.oribuin.arcade.api.game.ArcadeGame;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class GameRegistrationEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     */
    public GameRegistrationEvent() {
        super(!Bukkit.isPrimaryThread());
    }

    /**
     * Register a new arcade game into the plugin
     *
     * @param identifier The identifier for the game
     * @param supplier   The function to get the game
     * @param <T>        The arcade game type
     */
    public <T extends ArcadeGame<?>> Supplier<T> register(String identifier, @NotNull Supplier<T> supplier) {
        return GameRegistry.register(identifier, supplier);
    }

    /**
     * Get the handlers for this event class
     *
     * @return The handlers for this event class
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Get the handlers for this event class
     *
     * @return The handlers for this event class
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
