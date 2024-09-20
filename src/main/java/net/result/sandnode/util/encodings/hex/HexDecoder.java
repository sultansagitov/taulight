package net.result.sandnode.util.encodings.hex;

import net.result.sandnode.util.encodings.interfaces.IDecoder;
import org.jetbrains.annotations.NotNull;

public class HexDecoder implements IDecoder {

    @Override
    public byte[] decode(@NotNull String data) {
        if (data.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even length.");
        }

        int length = data.length();
        byte[] bytes = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            String hexPair = data.substring(i, i + 2);
            bytes[i / 2] = (byte) Integer.parseInt(hexPair, 16);
        }

        return bytes;
    }
}