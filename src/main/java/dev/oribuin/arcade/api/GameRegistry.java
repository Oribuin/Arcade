package dev.oribuin.arcade.api;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.api.game.GameInstance;
import dev.oribuin.arcade.manager.DataManager;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class GameRegistry {

    public static final Map<String, Supplier<? extends ArcadeGame<?>>> REGISTRY = new HashMap<>();
    public static final Map<UUID, ArcadeGame<?>> GAME_INSTANCES = new HashMap<>();

    /**
     * Register a new arcade game into the plugin
     *
     * @param identifier The identifier for the game
     * @param supplier   The function to get the game
     * @param <T>        The arcade game type
     */
    public static <T extends ArcadeGame<?>> Supplier<T> register(String identifier, @NotNull Supplier<T> supplier) {
        REGISTRY.put(identifier, supplier);

        DataManager dataManager = ArcadePlugin.getInstance().getDataManager();
        dataManager.loadGameInstances(identifier).thenAccept(instances -> {
            ArcadePlugin.getInstance().getLogger().info("Loading [" + instances.size() + "] instances of [" + identifier + "] into the world");
            for (GameInstance instance : instances) {
                PluginScheduler.get().runTaskAtLocation(instance.position(), () -> placeInstance(instance));
            }
        });
        
        return supplier;
    }

    /**
     * Get the game instance of the
     *
     * @param instance The game instances
     * @param <T>      The type of game
     * @return The resulting game
     */
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T getInstance(UUID instance) {
        return (T) GAME_INSTANCES.get(instance);
    }

    /**
     * Get the game that a user is currently participating in
     *
     * @param user The user potentially participating in the game
     * @param <T>  The type of game
     * @return The resulting game
     */
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T getParticipating(UUID user) {
        return GAME_INSTANCES.values().stream()
                .filter(x -> x.getParticipants().containsKey(user))
                .map(x -> (T) x)
                .findFirst()
                .orElse(null);
    }

    /**
     * Place a specified arcade game into the world with a set direction
     *
     * @param identifier The identifier of the game
     * @param position   The position of the game
     * @param direction  The direction of the game
     * @param <T>        The arcade game being placed
     * @return The placed game into the world
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T placeFreshGame(String identifier, Location position, BlockFace direction) {
        Supplier<? extends ArcadeGame<?>> game = REGISTRY.get(identifier);
        if (identifier == null) return null;

        ArcadeGame<?> result = game.get();
        result.place(position, direction);
        GAME_INSTANCES.put(result.getIdentifier(), result);
        ArcadePlugin.getInstance().getDataManager().saveGame(result);
        return (T) result;
    }

    /**
     * Place an existing arcade game into the world with a set direction
     *
     * @param instance The instance data of the game
     * @param <T>      The arcade game being placed
     * @return The placed game into the world
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends ArcadeGame<?>> T placeInstance(@NotNull GameInstance instance) {
        Supplier<? extends ArcadeGame<?>> game = REGISTRY.get(instance.name());
        if (game == null) return null;

        ArcadeGame<?> result = game.get();
        System.out.println("Instance Identifier: " + instance.identifier());
        result.setIdentifier(instance.identifier());
        System.out.println("Result Identifier: " + result.getIdentifier());
        result.place(instance.position(), instance.direction());
        GAME_INSTANCES.put(result.getIdentifier(), result);
        return (T) result;
    }


}
