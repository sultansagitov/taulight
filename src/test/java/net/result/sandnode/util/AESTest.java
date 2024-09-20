package net.result.sandnode.util;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.EncryptionFactory;
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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static net.result.sandnode.util.encryption.Encryption.*;

public class AESTest {

    @Test
    public void AESAfterRSAPlusEncodingTest() throws EncryptionException, DecryptionException, ReadingKeyException, NoSuchAlgorithmException, IOException {
        for (Encryption a : List.of(RSA)) {
            for (Encryption s : List.of(AES)) {
                final IEncryptor asymmetricEncryptor = EncryptionFactory.getEncryptor(a);
                final IDecryptor asymmetricDecryptor = EncryptionFactory.getDecryptor(a);
                final IEncryptor symmetricEncryptor = EncryptionFactory.getEncryptor(s);
                final IDecryptor symmetricDecryptor = EncryptionFactory.getDecryptor(s);

                final IGenerator asymmetricGenerator = EncryptionFactory.getGenerator(a);
                final IGenerator symmetricGenerator = EncryptionFactory.getGenerator(s);

                final AsymmetricKeyStorage asymmetricKeyStorage = (AsymmetricKeyStorage) asymmetricGenerator.generateKeyStorage();
                final SymmetricKeyStorage symmetricKeyStorage = (SymmetricKeyStorage) symmetricGenerator.generateKeyStorage();

                final byte[] bytes = symmetricKeyStorage.getKey().getEncoded();

                final byte[] encryptedBytes = asymmetricEncryptor.encryptBytes(bytes, asymmetricKeyStorage);
                Assertions.assertFalse(Arrays.equals(bytes, encryptedBytes));
                final byte[] decryptedBytes = asymmetricDecryptor.decryptBytes(encryptedBytes, asymmetricKeyStorage);

                Assertions.assertArrayEquals(bytes, decryptedBytes);

                final AESKeyStorage secondKeyStorage = AESKeyConvertor.toKeyStorage(decryptedBytes);
                final SecretKey AESToUse = secondKeyStorage.getKey();
                final IKeyStorage KSToUse = new AESKeyStorage().setKey(AESToUse);
                byte[] bytes2 = AESToUse.getEncoded();

                Assertions.assertArrayEquals(bytes, bytes2);

                final String data = "HelloString";
                final byte[] encryptedData = symmetricEncryptor.encrypt(data, KSToUse);
                final String decryptedData = symmetricDecryptor.decrypt(encryptedData, symmetricKeyStorage);

                Assertions.assertEquals(data, decryptedData);
            }
        }

    }

}
