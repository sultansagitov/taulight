package net.result.sandnode.util.encodings.no;

import net.result.sandnode.util.encodings.interfaces.IDecoder;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class NoDecoder implements IDecoder {

    @Override
    public byte[] decode(@NotNull String data) {
        return data.getBytes(US_ASCII);
    }
}
