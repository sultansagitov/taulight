package net.result.sandnode.util.encodings.no;

import net.result.sandnode.util.encodings.interfaces.IEncoder;
import org.jetbrains.annotations.NotNull;

public class NoEncoder implements IEncoder {

    @Override
    public String encode(@NotNull String data) {
        return data;
    }

    @Override
    public String encode(byte @NotNull [] data) {
        return new String(data);
    }
}
