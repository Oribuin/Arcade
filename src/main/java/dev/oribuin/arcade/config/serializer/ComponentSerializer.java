package dev.oribuin.arcade.config.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class ComponentSerializer extends ScalarSerializer<Component> {

    private static final ComponentSerializer INSTANCE = new ComponentSerializer();

    private ComponentSerializer() {
        super(Component.class);
    }

    @Override
    public Component deserialize(Type type, Object obj) throws SerializationException {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(obj.toString());
    }

    @Override
    protected Object serialize(Component item, Predicate<Class<?>> typeSupported) {
        return MiniMessage.miniMessage().serialize(item);
    }

    public static ComponentSerializer getInstance() {
        return INSTANCE;
    }


}
