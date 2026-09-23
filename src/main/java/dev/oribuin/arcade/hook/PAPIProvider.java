package dev.oribuin.arcade.hook;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.manager.DataManager;
import dev.oribuin.arcade.statistic.GameStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIProvider extends PlaceholderExpansion {

    private final ArcadePlugin plugin;

    public PAPIProvider(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Parse the placeholders when the player uses
     *
     * @param player The player requesting the placeholders
     * @param params The param for the placeholders
     * @return The resulting placeholders
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        String[] split = params.split("_");
        String first = split[0];
        String secondary = split.length > 1 ? params.substring(first.length() + 1) : null;

        if (secondary == null) {
            return "please specify a game (e.g. arcade_wins_connect_four";
        }

        DataManager manager = this.plugin.getDataManager();
        GameStats stats = manager.getStats(player.getUniqueId(), secondary);
    
        return switch (first.toLowerCase()) {
            case "wins" -> String.valueOf(stats.getWins());
            case "loss" -> String.valueOf(stats.getLosses());
            case "played" -> String.valueOf(stats.getPlays());
            case "winrate" -> String.format("%.2f", ((double) (stats.getWins() / stats.getPlays())) * 100);
            default -> "Unexpected value: " + first.toLowerCase();
        };
    }

    /**
     * The placeholder identifier of this expansion. May not contain {@literal %},
     * {@literal {}} or _
     *
     * @return placeholder identifier that is associated with this expansion
     */
    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getPluginMeta().getName();
    }

    /**
     * The author of this expansion
     *
     * @return name of the author for this expansion
     */
    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", this.plugin.getPluginMeta().getAuthors());
    }

    /**
     * The version of this expansion
     *
     * @return current version of this expansion
     */
    @Override
    public @NotNull String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    /**
     * Expansions that do not use the ecloud and instead register from the dependency should set this
     * to true to ensure that your placeholder expansion is not unregistered when the papi reload
     * command is used
     *
     * @return if this expansion should persist through placeholder reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * If any requirements need to be checked before this expansion should register, you can check
     * them here
     *
     * @return true if this hook meets all the requirements to register
     */
    @Override
    public boolean canRegister() {
        return true;
    }

}
