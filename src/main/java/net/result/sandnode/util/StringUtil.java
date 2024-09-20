package net.result.sandnode.util;

import org.jetbrains.annotations.NotNull;

public class StringUtil {

    public static @NotNull String capitalize(@NotNull String text) {
        if (text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

}
