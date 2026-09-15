package dev.oribuin.arcade.api.game;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Create a new game instance identifier used to create, load and delete games from the plugin
 *
 * @param identifier The arcade game uuid
 * @param name       The name for the game
 * @param position   The position of the game
 * @param direction  The location of the game
 */
public record GameInstance(UUID identifier, String name, Location position, BlockFace direction) {

    /**
     * Construct a game instance from an arcade plugin
     *
     * @param instance The arcade game in the world
     * @param <T>      The arcade game instance
     * @return The game instance
     */
    public static <T extends ArcadeGame<?>> GameInstance construct(@NotNull T instance) {
        return new GameInstance(
                instance.getIdentifier(),
                instance.getName(),
                instance.getLocation(),
                instance.getDirection()
        );
    }

    @Override
    public String toString() {
        return "GameInstance{" +
                "identifier=" + identifier +
                ", name='" + name + '\'' +
                ", position=" + position +
                ", direction=" + direction +
                '}';
    }
}
