package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

import static net.result.sandnode.util.encryption.Encryption.AES;

public class AESKeyStorage extends SymmetricKeyStorage {
    public AESKeyStorage(@NotNull SecretKey key) {
        this.key = key;
    }

    public AESKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.setKey(globalKeyStorage.getSymmetric(AES).getKey());
    }

    @Override
    public Encryption encryption() {
        return AES;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new AESKeyStorage(key);
    }

    @Override
    public @NotNull AESKeyStorage setKey(@NotNull SecretKey key) {
        this.key = key;
        return this;
    }
}
