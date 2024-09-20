package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

public class AESKeyStorage extends SymmetricKeyStorage {
    public AESKeyStorage() {
    }

    public AESKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.setKey(globalKeyStorage.getAESKeyStorage().key);
    }

    public @NotNull AESKeyStorage setKey(@NotNull SecretKey key) {
        this.key = key;
        return this;
    }
}
