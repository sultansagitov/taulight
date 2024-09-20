package net.result.sandnode.util.encodings.hex;

import net.result.sandnode.util.encodings.interfaces.IEncoder;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class HexEncoder implements IEncoder {

    @Override
    public String encode(@NotNull String data) {
        return encode(data.getBytes(US_ASCII));
    }

    @Override
    public String encode(byte @NotNull [] data) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}
