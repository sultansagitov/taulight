package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricKeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class HybridEncryptionTest {
    @Test
    public void hybridEncryptionTest() throws Exception {
        for (IAsymmetricEncryption a : EncryptionManager.getAsymmetric()) {
            for (ISymmetricEncryption s : EncryptionManager.getSymmetric()) {
                IAsymmetricKeyStorage asymmetricKeyStorage = a.generate();
                ISymmetricKeyStorage symmetricKeyStorage = s.generate();

                byte[] bytes = symmetricKeyStorage.toBytes();

                byte[] encryptedBytes;
                byte[] decryptedBytes;
                try {
                    encryptedBytes = a.encryptBytes(bytes, asymmetricKeyStorage);
                    decryptedBytes = a.decryptBytes(encryptedBytes, asymmetricKeyStorage);
                } catch (CannotUseEncryption e) {
                    throw new ImpossibleRuntimeException(e);
                }

                Assertions.assertFalse(Arrays.equals(bytes, encryptedBytes));
                Assertions.assertArrayEquals(bytes, decryptedBytes);

                ISymmetricKeyStorage secondKeyStorage = s.toKeyStorage(decryptedBytes);

                try {
                    String data = "HelloString";
                    byte[] encryptedData = s.encrypt(data, secondKeyStorage);
                    String decryptedData = s.decrypt(encryptedData, symmetricKeyStorage);
                    Assertions.assertEquals(data, decryptedData);
                } catch (CannotUseEncryption e) {
                    throw new ImpossibleRuntimeException(e);
                }

            }
        }
    }
}
