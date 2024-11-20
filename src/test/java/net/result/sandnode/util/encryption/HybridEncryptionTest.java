package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encryption.interfaces.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static net.result.sandnode.util.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.util.encryption.SymmetricEncryption.AES;

public class HybridEncryptionTest {
    @Test
    public void hybridEncryptionTest() throws EncryptionException, DecryptionException, ReadingKeyException,
            CannotUseEncryption {
        for (IAsymmetricEncryption a : List.of(RSA)) {
            for (ISymmetricEncryption s : List.of(AES)) {
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
