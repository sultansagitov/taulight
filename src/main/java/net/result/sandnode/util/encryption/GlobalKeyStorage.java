package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalKeyStorage {
    private RSAKeyStorage rsaKeyStorage = null;
    private AESKeyStorage aesKeyStorage = null;

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
