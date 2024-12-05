package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class HybridEncryptionTest {
    @Test
    public void hybridEncryptionTest() throws EncryptionException, DecryptionException {
        for (IAsymmetricEncryption a : Encryptions.getAsymmetric()) {
            for (ISymmetricEncryption s : Encryptions.getSymmetric()) {
                IEncryptor asymmetricEncryptor = a.encryptor();
                IDecryptor asymmetricDecryptor = a.decryptor();
                IEncryptor symmetricEncryptor = s.encryptor();
                IDecryptor symmetricDecryptor = s.decryptor();

                IGenerator asymmetricGenerator = a.generator();
                IGenerator symmetricGenerator = s.generator();

                ISymmetricKeyConvertor symmetricKeyConvertor = s.keyConvertor();

                IAsymmetricKeyStorage asymmetricKeyStorage = (IAsymmetricKeyStorage) asymmetricGenerator.generate();
                ISymmetricKeyStorage symmetricKeyStorage = (ISymmetricKeyStorage) symmetricGenerator.generate();

                byte[] bytes = symmetricKeyConvertor.toBytes(symmetricKeyStorage);

                byte[] encryptedBytes = asymmetricEncryptor.encryptBytes(bytes, asymmetricKeyStorage);
               Assertions.assertFalse(Arrays.equals(bytes, encryptedBytes));
                byte[] decryptedBytes = asymmetricDecryptor.decryptBytes(encryptedBytes, asymmetricKeyStorage);

                Assertions.assertArrayEquals(bytes, decryptedBytes);

                ISymmetricKeyStorage secondKeyStorage = symmetricKeyConvertor.toKeyStorage(decryptedBytes);

                String data = "HelloString";
                byte[] encryptedData = symmetricEncryptor.encrypt(data, secondKeyStorage);
                String decryptedData = symmetricDecryptor.decrypt(encryptedData, symmetricKeyStorage);

                Assertions.assertEquals(data, decryptedData);
            }
        }
    }
}
