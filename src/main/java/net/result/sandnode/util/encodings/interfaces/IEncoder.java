package net.result.sandnode.util.encodings.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IEncoder {
    String encode(@NotNull String data);

    String encode(byte @NotNull [] data);
}
