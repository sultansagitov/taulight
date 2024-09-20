package net.result.sandnode.util.encryption.no;

import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class NoEncryptor implements IEncryptor {

    @Override
    public byte @NotNull [] encrypt(@NotNull String data, @Nullable IKeyStorage keyStorage) {
        return data.getBytes(US_ASCII);
    }

    @Override
    public byte @NotNull [] encryptBytes(byte @NotNull [] data, @Nullable IKeyStorage keyStorage) {
        return data;
    }

}
