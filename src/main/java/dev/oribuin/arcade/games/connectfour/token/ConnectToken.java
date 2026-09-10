package dev.oribuin.arcade.games.connectfour.token;

import dev.oribuin.arcade.games.connectfour.ConnectGame;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class ConnectToken {

    public static final float TOKEN_SIZE = 0.35f;

    private final BlockDisplay display;
    private TokenColour colour;

    public ConnectToken(TokenColour colour, BlockDisplay display) {
        this.colour = colour;
        this.display = display;
    }

    /**
     * Update the display icon for the token
     */
    public void update() {
        if (this.display == null || ConnectGame.CONNECT_TOKEN == null) return;
        Transformation transformation = new Transformation(
                new Vector3f(-(TOKEN_SIZE / 2), 0, -(TOKEN_SIZE / 2)),
                new AxisAngle4f(),
                new Vector3f(TOKEN_SIZE, TOKEN_SIZE, TOKEN_SIZE),
                new AxisAngle4f()
        );

        this.display.setBlock(this.colour.getIcon().createBlockData());
        this.display.setBrightness(new Display.Brightness(15, 15));
        this.display.setDisplayHeight(TOKEN_SIZE);
        this.display.setDisplayWidth(TOKEN_SIZE);
        this.display.setTransformation(transformation);
        this.display.setBillboard(Display.Billboard.FIXED);

        // Mark the entity as a game
        PersistentDataContainer container = this.display.getPersistentDataContainer();
        container.set(ConnectGame.CONNECT_TOKEN, PersistentDataType.INTEGER, 0);
    }


    public BlockDisplay getDisplay() {
        return display;
    }

    public TokenColour getColour() {
        return colour;
    }

    public void setColour(TokenColour colour) {
        this.colour = colour;
    }

}
 