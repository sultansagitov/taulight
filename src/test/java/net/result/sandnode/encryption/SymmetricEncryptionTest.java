package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SymmetricEncryptionTest {
    @Test
    public void toBytes() {
        for (SymmetricEncryption s : EncryptionManager.getSymmetric()) {
            SymmetricKeyStorage keyStorage = s.generate();
            byte[] bytes1 = keyStorage.toBytes();
            byte[] bytes2 = s.toKeyStorage(bytes1).toBytes();
            Assertions.assertArrayEquals(bytes1, bytes2);
        }
    }
}