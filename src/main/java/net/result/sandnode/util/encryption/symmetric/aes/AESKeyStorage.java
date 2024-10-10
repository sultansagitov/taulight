package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

import static net.result.sandnode.util.encryption.Encryption.AES;

public class AESKeyStorage extends SymmetricKeyStorage {
    private static final AESKeyStorage instance = new AESKeyStorage();

    private AESKeyStorage() {}

    public AESKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.setKey(globalKeyStorage.getAESKeyStorage().key);
    }

    @Override
    public Encryption encryption() {
        return AES;
    }

    public static AESKeyStorage getInstance() {
        return instance;
    }

    @Override
    public @NotNull AESKeyStorage setKey(@NotNull SecretKey key) {
        this.key = key;
        return this;
    }
}
