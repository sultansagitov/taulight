package net.result.sandnode.util;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.List;

import static net.result.sandnode.util.encryption.Encryption.AES;
import static net.result.sandnode.util.encryption.Encryption.RSA;

public class AESTest {

    @Test
    public void AESAfterRSAPlusEncodingTest() throws EncryptionException, DecryptionException, ReadingKeyException {
        for (Encryption a : List.of(RSA)) {
            for (Encryption s : List.of(AES)) {
                IEncryptor asymmetricEncryptor = a.encryptor();
                IDecryptor asymmetricDecryptor = a.decryptor();
                IEncryptor symmetricEncryptor = s.encryptor();
                IDecryptor symmetricDecryptor = s.decryptor();

                IGenerator asymmetricGenerator = a.generator();
                IGenerator symmetricGenerator = s.generator();

                AsymmetricKeyStorage asymmetricKeyStorage = (AsymmetricKeyStorage) asymmetricGenerator.generateKeyStorage();
                SymmetricKeyStorage symmetricKeyStorage = (SymmetricKeyStorage) symmetricGenerator.generateKeyStorage();

                byte[] bytes = symmetricKeyStorage.getKey().getEncoded();

                byte[] encryptedBytes = asymmetricEncryptor.encryptBytes(bytes, asymmetricKeyStorage);
                Assertions.assertFalse(Arrays.equals(bytes, encryptedBytes));
                byte[] decryptedBytes = asymmetricDecryptor.decryptBytes(encryptedBytes, asymmetricKeyStorage);

                Assertions.assertArrayEquals(bytes, decryptedBytes);

                AESKeyStorage secondKeyStorage = AESKeyConvertor.getInstance().toKeyStorage(decryptedBytes);
                SecretKey AESToUse = secondKeyStorage.getKey();
                IKeyStorage KSToUse = new AESKeyStorage(AESToUse);
                byte[] bytes2 = AESToUse.getEncoded();

                Assertions.assertArrayEquals(bytes, bytes2);

                String data = "HelloString";
                byte[] encryptedData = symmetricEncryptor.encrypt(data, KSToUse);
                String decryptedData = symmetricDecryptor.decrypt(encryptedData, symmetricKeyStorage);

                Assertions.assertEquals(data, decryptedData);
            }
        }

    }

}
