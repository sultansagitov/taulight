package net.result.sandnode.encryption.aes;

import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

import static net.result.sandnode.encryption.SymmetricEncryption.AES;

public record AESKeyStorage(SecretKey key) implements SymmetricKeyStorage {
    public AESKeyStorage(@NotNull SecretKey key) {
        this.key = key;
    }

    @Override
    public @NotNull KeyStorage copy() {
        return new AESKeyStorage(key);
    }

    @Override
    public @NotNull SecretKey key() {
        return this.key;
    }

    @Override
    public @NotNull ISymmetricEncryption encryption() {
        return AES;
    }

    @Override
    public byte[] toBytes() {
        return AESKeyConvertor.toBytes(this);
    }
}
