package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;

public class GlobalKeyStorage {
    private RSAKeyStorage rsaKeyStorage = null;
    private AESKeyStorage aesKeyStorage = null;

    public RSAKeyStorage getRSAKeyStorage() {
        return rsaKeyStorage;
    }

    public void setRSAKeyStorage(RSAKeyStorage rsaKeyStorage) {
        this.rsaKeyStorage = rsaKeyStorage;
    }

    public AESKeyStorage getAESKeyStorage() {
        return aesKeyStorage;
    }

    public void setAESKeyStorage(AESKeyStorage aesKeyStorage) {
        this.aesKeyStorage = aesKeyStorage;
    }
}
