package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

public class AESKeyStorage extends SymmetricKeyStorage {
    private static final AESKeyStorage instance = new AESKeyStorage();

    private AESKeyStorage() {}

    public AESKeyStorage getInstance() {
        return instance;
    }

    public AESKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.setKey(globalKeyStorage.getAESKeyStorage().key);
    }

    public @NotNull AESKeyStorage setKey(@NotNull SecretKey key) {
        this.key = key;
        return this;
    }
}
