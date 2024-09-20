package net.result.sandnode.util.encryption.no;

import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoDecryptor implements IDecryptor {
    @Override
    public String decrypt(byte @NotNull [] data, @Nullable IKeyStorage ks) {
        return new String(data);
    }

    @Override
    public byte[] decryptBytes(byte @NotNull [] data, @Nullable IKeyStorage ks) {
        return data;
    }
}
