package dev.oribuin.arcade.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ArcadeUtils {

    public static PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    public static MiniMessage MINIMESSAGE = MiniMessage.miniMessage();

    public static TagResolver RESOLVER = TagResolver.builder()
            .resolvers(
                    StandardTags.color(),
                    StandardTags.gradient(),
                    StandardTags.decorations(),
                    StandardTags.clickEvent(),
                    StandardTags.hoverEvent(),
                    StandardTags.shadowColor(),
                    StandardTags.reset(),
                    StandardTags.font(),
                    StandardTags.rainbow(),
                    StandardTags.nbt(),
                    StandardTags.pride()
            )
            .build();


    public ArcadeUtils() {
        throw new IllegalStateException("FishUtil is a utility class and cannot be instantiated.");
    }

    /**
     * Convert a string to a component
     *
     * @param text The text to convert
     * @return The component
     */
    public static Component kyorify(String text) {
        return MINIMESSAGE.deserialize(text, RESOLVER)
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Convert a string into a component with placeholders applied
     *
     * @param text         The text to convert
     * @param placeholders The placeholders to apply
     * @return The component
     */
    public static Component kyorify(String text, Placeholders placeholders) {
        return placeholders.apply(text);
    }

    /**
     * Get a bukkit color from a hex code
     *
     * @param hex The hex code
     * @return The bukkit color
     */
    public static Color fromHex(String hex) {
        if (hex == null)
            return Color.BLACK;

        java.awt.Color awtColor;
        try {
            awtColor = java.awt.Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }

        return Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    /**
     * Parse an integer from an object safely
     *
     * @param object The object
     * @return The integer
     */
    private static int toInt(String object) {
        try {
            return Integer.parseInt(object);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Convert a string to a duration
     *
     * @param input The input string
     * @return The duration
     */
    public static Duration getTime(String input) {
        if (input == null || input.isEmpty()) return Duration.ZERO;

        long seconds = 0;
        long minutes = 0;
        long hours = 0;
        long days = 0;

        String[] split = input.split(" ");
        for (String s : split) {
            if (s.endsWith("s")) {
                seconds += toInt(s.replace("s", ""));
            } else if (s.endsWith("m")) {
                minutes += toInt(s.replace("m", ""));
            } else if (s.endsWith("h")) {
                hours += toInt(s.replace("h", ""));
            } else if (s.endsWith("d")) {
                days += toInt(s.replace("d", ""));
            }
        }

        return Duration.ofSeconds(seconds).plusMinutes(minutes).plusHours(hours).plusDays(days);

    }

    /**
     * Format a time in milliseconds into a string
     *
     * @param time Time in milliseconds
     * @return Formatted time
     */
    public static String formatTime(long time) {
        long totalSeconds = time / 1000;
        if (totalSeconds <= 0) return "";

        long days = (int) Math.floor(totalSeconds / 86400.0);
        totalSeconds %= 86400;

        long hours = (int) Math.floor(totalSeconds / 3600.0);
        totalSeconds %= 3600;

        long minutes = (int) Math.floor(totalSeconds / 60.0);
        long seconds = (totalSeconds % 60);

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder.append(days).append("d, ");
        if (hours > 0) builder.append(hours).append("h, ");
        if (minutes > 0) builder.append(minutes).append("m, ");
        if (seconds > 0) builder.append(seconds).append("s");

        // remove the last comma
        if (builder.toString().endsWith(", ")) {
            builder.setLength(builder.length() - 2);
        }

        return builder.toString();
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name) {
        if (name == null)
            return null;

        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }

    /**
     * Get an enum from a string value
     *
     * @param enumClass The enum class
     * @param name      The name of the enum
     * @param <T>       The enum name
     * @return The enum
     */
    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name, T def) {
        if (name == null)
            return def;

        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        return def;
    }

    /**
     * Convert a list of strings to a list of enums
     *
     * @param enumClass The enum class
     * @param content   The content to convert
     * @param <T>       The enum type
     * @return The list of enums
     */
    public static <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, List<String> content) {
        return content.stream()
                .map(s -> getEnum(enumClass, s))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T extends Enum<T>> String niceify(T enumValue) {
        String noUnderscores = enumValue.name().toLowerCase().replace("_", " ");
        return StringUtils.capitalize(noUnderscores);
    }

    public static String niceify(String text) {
        String noUnderscores = text.toLowerCase().replace("_", " ");
        return Arrays.stream(noUnderscores.split(" "))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    public static <T extends Enum<T>> String niceify(T enumValue, String def) {
        if (enumValue == null) return def;

        String noUnderscores = enumValue.name().toLowerCase().replace("_", " ");
        return StringUtils.capitalize(noUnderscores);
    }

    /**
     * Format every word in a string to be capitalized
     *
     * @param str The string to format
     * @return The formatted string
     */
    public static String capitalizeFully(String str) {
        String[] split = str.toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();

        for (String s : split) {
            builder.append(StringUtils.capitalize(s)).append(" ");
        }

        return builder.toString().trim();
    }

}
