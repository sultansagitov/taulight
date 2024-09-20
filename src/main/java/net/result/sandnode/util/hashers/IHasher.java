package net.result.sandnode.util.hashers;

import org.jetbrains.annotations.NotNull;

public interface IHasher {
    @NotNull String hash(@NotNull String data);

    @NotNull String hash(byte @NotNull [] data);
}
