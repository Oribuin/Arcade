package dev.oribuin.arcade.games.connectfour.token;

import org.bukkit.Color;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record TokenColour(Material icon, Color color) {

    public static final Map<String, TokenColour> COLORS = new HashMap<>();

    // region Playable Colours
    public static final @NotNull TokenColour RED = register("red", Material.RED_CONCRETE, Color.RED);
    public static final @NotNull TokenColour ORANGE = register("orange", Material.ORANGE_CONCRETE, Color.ORANGE);
    public static final @NotNull TokenColour YELLOW = register("yellow", Material.YELLOW_CONCRETE, Color.YELLOW);
    public static final @NotNull TokenColour LIME = register("lime", Material.LIME_CONCRETE, Color.LIME);
    public static final @NotNull TokenColour GREEN = register("green", Material.GREEN_CONCRETE, Color.GREEN);
    public static final @NotNull TokenColour AQUA = register("aqua", Material.LIGHT_BLUE_CONCRETE, Color.AQUA);
    public static final @NotNull TokenColour CYAN = register("cyan", Material.CYAN_CONCRETE, Color.TEAL);
    public static final @NotNull TokenColour BLUE = register("blue", Material.BLUE_CONCRETE, Color.BLUE);
    public static final @NotNull TokenColour PURPLE = register("purple", Material.PURPLE_CONCRETE, Color.PURPLE);
    public static final @NotNull TokenColour PINK = register("pink", Material.PINK_CONCRETE, Color.FUCHSIA);
    // endregion

    public static TokenColour EMPTY = new TokenColour(Material.WHITE_CONCRETE, Color.GRAY);

    /**
     * Register a playable token colour into the plugin, but not null because i know what im doing.
     *
     * @param id       The token colour
     * @param material The token material
     * @param color    The colour of the glow effect
     * @return The token colour
     */
    @NotNull
    private static TokenColour register(@NotNull String id, @NotNull Material material, @NotNull Color color) {
        TokenColour token = new TokenColour(material, color);
        COLORS.put(id, token);
        return token;
    }

    /**
     * Register a playable token colour into the plugin
     *
     * @param id       The token colour
     * @param material The token material
     * @param color    The colour of the glow effect
     * @return The token colour
     */
    @Nullable
    public static TokenColour registerColour(@NotNull String id, @NotNull Material material, @NotNull Color color) {
        TokenColour token = new TokenColour(material, color);
        if (!material.isBlock()) return null;

        COLORS.put(id, token);
        return token;
    }

    /**
     * Get a new token colour from the plugin
     *
     * @param id The id of the token
     * @return The resulting colour
     */
    @NotNull
    public static TokenColour from(@NotNull String id) {
        return COLORS.getOrDefault(id, EMPTY);
    }


}
