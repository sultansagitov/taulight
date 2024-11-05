package net.result.sandnode.util.encryption.core.aes;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

import static net.result.sandnode.util.encryption.Encryption.AES;

public class AESKeyStorage extends SymmetricKeyStorage {
    public AESKeyStorage(@NotNull SecretKey key) {
        this.key = key;
    }

    @Override
    public @NotNull Encryption encryption() {
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
