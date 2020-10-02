package io.github.nucleuspowered.nucleus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

public final class AdventureUtils {

    public static boolean isEmpty(final Component component) {
        return component == Component.empty();
    }

    public static String getContent(final Component component) {
        return PlainComponentSerializer.plain().serialize(component);
    }

}
