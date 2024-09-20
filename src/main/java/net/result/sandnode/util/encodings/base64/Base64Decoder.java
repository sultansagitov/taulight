package net.result.sandnode.util.encodings.base64;

import net.result.sandnode.util.encodings.interfaces.IDecoder;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

public class Base64Decoder implements IDecoder {

    @Override
    public byte[] decode(@NotNull String data) {
        return Base64.getDecoder().decode(data);
    }

}
