package net.result.sandnode.hasher;

import org.jetbrains.annotations.NotNull;

public interface Hasher {
    @NotNull String hash(@NotNull String data);

    String name();
}
