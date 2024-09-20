package net.result.sandnode.util.encodings.base64;

import net.result.sandnode.util.encodings.interfaces.IEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class Base64Encoder implements IEncoder {

    @Override
    public String encode(@NotNull String data) {
        return encode(data.getBytes(US_ASCII));
    }

    @Override
    public String encode(byte @NotNull [] data) {
        return Base64.getEncoder().encodeToString(data);
    }

}
