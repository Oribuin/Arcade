package dev.oribuin.arcade.games.connectfour.token;

import org.bukkit.Color;
import org.bukkit.Material;

public enum TokenColour {
    RED(Material.RED_CONCRETE, Color.RED),
    ORANGE(Material.ORANGE_CONCRETE, Color.ORANGE),
    YELLOW(Material.YELLOW_CONCRETE, Color.YELLOW),
    LIME(Material.LIME_CONCRETE, Color.LIME),
    GREEN(Material.GREEN_CONCRETE, Color.GREEN),
    AQUA(Material.LIGHT_BLUE_CONCRETE, Color.AQUA),
    CYAN(Material.CYAN_CONCRETE, Color.TEAL),
    BLUE(Material.BLUE_CONCRETE, Color.BLUE),
    PURPLE(Material.PURPLE_CONCRETE, Color.PURPLE),
    PINK(Material.PINK_CONCRETE, Color.FUCHSIA),

    EMPTY(Material.WHITE_CONCRETE, Color.GRAY);

    private final Material icon;
    private final Color color;

    TokenColour(Material icon, Color color) {
        this.icon = icon;
        this.color = color;
    }

    public Material getIcon() {
        return icon;
    }

    public Color getColor() {
        return color;
    }
}
