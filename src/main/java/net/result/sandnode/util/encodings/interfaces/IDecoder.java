package net.result.sandnode.util.encodings.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IDecoder {

    byte[] decode(@NotNull String data);

}
