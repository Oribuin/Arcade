package dev.oribuin.arcade.games.connectfour.token;

import dev.oribuin.arcade.games.connectfour.ConnectGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class ConnectToken {

    public static final float TOKEN_SIZE = 0.35f;

    private UUID displayId;
    private TokenColour colour;

    public ConnectToken(TokenColour colour, UUID display) {
        this.colour = colour;
        this.displayId = display;
    }

    /**
     * Update the display icon for the token
     *
     * @param display The display to update
     */
    public void update(@NotNull BlockDisplay display) {
        apply(display, this.colour);
    }

    /**
     * Update the display icon for the token
     *
     * @param display The display to update
     * @param colour  The colour of the token
     */
    public static void apply(@NotNull BlockDisplay display, @NotNull TokenColour colour) {
        Transformation transformation = new Transformation(
                new Vector3f(-(TOKEN_SIZE / 2), 0, -(TOKEN_SIZE / 2)),
                new AxisAngle4f(),
                new Vector3f(TOKEN_SIZE, TOKEN_SIZE, TOKEN_SIZE),
                new AxisAngle4f()
        );

        display.setBlock(colour.icon().createBlockData());
        display.setBrightness(new Display.Brightness(15, 15));
        display.setDisplayHeight(TOKEN_SIZE);
        display.setDisplayWidth(TOKEN_SIZE);
        display.setTransformation(transformation);
        display.setBillboard(Display.Billboard.FIXED);

        // Mark the entity as a game
        PersistentDataContainer container = display.getPersistentDataContainer();
        container.set(ConnectGame.CONNECT_TOKEN, PersistentDataType.INTEGER, 0);
    }

    @Nullable
    public BlockDisplay getDisplay() {
        return Bukkit.getEntity(this.displayId) instanceof BlockDisplay display ? display : null;
    }

    public void setDisplayId(UUID displayId) {
        this.displayId = displayId;
    }

    public UUID getDisplayId() {
        return displayId;
    }

    public TokenColour getColour() {
        return colour;
    }

    public void setColour(TokenColour colour) {
        this.colour = colour;
    }

}
 