package net.result.sandnode.util.encryption.none;

import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoneDecryptor implements IDecryptor {

    private static final NoneDecryptor INSTANCE = new NoneDecryptor();

    public static NoneDecryptor instance() {
        return INSTANCE;
    }

    @Override
    public String decrypt(byte @NotNull [] data, @Nullable IKeyStorage ks) {
        return new String(data);
    }

    @Override
    public byte[] decryptBytes(byte @NotNull [] data, @Nullable IKeyStorage ks) {
        return data;
    }

}
