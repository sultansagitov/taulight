package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalKeyStorage {
    private RSAKeyStorage rsaKeyStorage = null;
    private AESKeyStorage aesKeyStorage = null;

    public @Nullable IKeyStorage getKeyStorage(
            @NotNull Encryption encryption
    ) {
        return switch (encryption) {
            case RSA -> new RSAKeyStorage(this);
            case AES -> new AESKeyStorage(this);
            case NO -> null;
        };
    }

    public void setKeyStorage(
            @NotNull Encryption encryption,
            @NotNull IKeyStorage keyStorage
    ) {
        switch (encryption) {
            case RSA: {
                if (keyStorage instanceof RSAKeyStorage) {
                    this.setRSAKeyStorage((RSAKeyStorage) keyStorage);
                }
                break;
            }
            case AES: {
                if (keyStorage instanceof AESKeyStorage) {
                    this.setAESKeyStorage((AESKeyStorage) keyStorage);
                }
                break;
            }
        }
    }

    public @Nullable RSAKeyStorage getRSAKeyStorage() {
        return rsaKeyStorage;
    }

    public void setRSAKeyStorage(@NotNull RSAKeyStorage rsaKeyStorage) {
        this.rsaKeyStorage = rsaKeyStorage;
    }

    public @Nullable AESKeyStorage getAESKeyStorage() {
        return aesKeyStorage;
    }

    public void setAESKeyStorage(@NotNull AESKeyStorage aesKeyStorage) {
        this.aesKeyStorage = aesKeyStorage;
    }

    public GlobalKeyStorage copy() {
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        if (this.getAESKeyStorage() != null) globalKeyStorage.setAESKeyStorage(this.getAESKeyStorage());
        if (this.getRSAKeyStorage() != null) globalKeyStorage.setRSAKeyStorage(this.getRSAKeyStorage());
        return globalKeyStorage;
    }
}
