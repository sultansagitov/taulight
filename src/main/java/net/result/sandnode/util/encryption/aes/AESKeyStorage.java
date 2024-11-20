package net.result.sandnode.util.encryption.aes;

import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

import static net.result.sandnode.util.encryption.SymmetricEncryption.AES;

public class AESKeyStorage implements ISymmetricKeyStorage {
    protected final SecretKey key;

    public AESKeyStorage(@NotNull SecretKey key) {
        this.key = key;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new AESKeyStorage(key);
    }

    public @NotNull SecretKey getKey() {
        return this.key;
    }

    @Override
    public @NotNull ISymmetricEncryption encryption() {
        return AES;
    }
}
