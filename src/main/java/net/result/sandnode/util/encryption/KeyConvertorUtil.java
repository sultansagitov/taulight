package net.result.sandnode.util.encryption;

import org.jetbrains.annotations.NotNull;

public class KeyConvertorUtil {

    public static @NotNull String makePEM(@NotNull String base64Key, @NotNull String keyType) {
        return "-----BEGIN %s-----\n%s\n-----END %s-----".formatted(
                        keyType,
                        base64Key.replaceAll("(.{64})", "$1\n"),
                        keyType
                )
                .replaceAll("\\n\\n", "\n")
                .trim();
    }

    public static @NotNull String removePEM(@NotNull String pemString) {
        return pemString
                .replaceAll("\\r?\\n", "")
                .replaceAll("-----(BEGIN|END) [^-]+-----", "")
                .trim();
    }

    public static @NotNull String analyzePemString(@NotNull String pemString) {
        String[] lines = pemString.trim().split("\n");

        for (String line : lines)
            if (line.startsWith("-----BEGIN "))
                return line.substring(11, line.indexOf("-----", 11)).trim();

        return "UNKNOWN";
    }

}
