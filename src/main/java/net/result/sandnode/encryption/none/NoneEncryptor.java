package net.result.sandnode.encryption.none;

import net.result.sandnode.encryption.interfaces.IEncryptor;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class NoneEncryptor implements IEncryptor {

    private static final NoneEncryptor INSTANCE = new NoneEncryptor();

    public static NoneEncryptor instance() {
        return INSTANCE;
    }

    @Override
    public byte @NotNull [] encrypt(@NotNull String data, @Nullable IKeyStorage keyStorage) {
        return data.getBytes(US_ASCII);
    }

    @Override
    public byte @NotNull [] encryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) {
        return data;
    }

}
