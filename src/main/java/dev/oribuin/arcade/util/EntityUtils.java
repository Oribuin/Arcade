package dev.oribuin.arcade.util;

import dev.oribuin.arcade.scheduler.PluginScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public final class EntityUtils {

    /**
     * Modify an entity via their uuid using Folia-Safe Methods
     *
     * @param type The type of entity to modify 
     * @param uniqueId The entity uuid 
     * @param action The action to run if the entity is available
     * @param <T> The type of entity
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> void safelyModify(
            @NotNull Class<T> type,
            @NotNull UUID uniqueId,
            @NotNull Consumer<T> action
    ) {
        Entity entity = Bukkit.getEntity(uniqueId);
        if (entity == null || !entity.isValid() || entity.isDead()) return; // Cannot modify the entity
        
        PluginScheduler.get().runTaskAtEntity(entity, () -> action.accept((T) entity));
    }
    
    /**
     * Modify an entity via their uuid using Folia-Safe Methods
     *
     * @param uniqueId The entity uuid 
     * @param action The action to run if the entity is available
     */
    public static void safelyModify(
            @NotNull UUID uniqueId,
            @NotNull Consumer<Entity> action
    ) {
        Entity entity = Bukkit.getEntity(uniqueId);
        if (entity == null || !entity.isValid() || entity.isDead()) return; // Cannot modify the entity

        PluginScheduler.get().runTaskAtEntity(entity, () -> action.accept(entity));
    }

    /**
     * Modify an entity via the instance using Folia-Safe Methods
     *
     * @param action The action to run if the entity is available
     * @param <T> The type of entity
     */
    public static <T extends Entity> void safelyModify(@NotNull T entity, @NotNull Consumer<T> action) {
        PluginScheduler.get().runTaskAtEntity(entity, () -> action.accept(entity));
    }

}
