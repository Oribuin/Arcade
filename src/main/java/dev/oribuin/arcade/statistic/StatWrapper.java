package dev.oribuin.arcade.statistic;

import dev.oribuin.arcade.api.GameRegistry;

import java.util.HashMap;
import java.util.Map;

public record StatWrapper(Map<String, GameStats> stats) {

    public StatWrapper() {
        this(getEmptyStats());
    }

    public static Map<String, GameStats> getEmptyStats() {
        Map<String, GameStats> stats = new HashMap<>();
        GameRegistry.get().getRegistry().keySet().forEach(s -> stats.put(s, new GameStats()));
        return stats;
    }


}
