package net.result.sandnode.util.encryption;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.util.encryption.Encryption.*;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptionTest {

    @Test
    public void testEncryptionDecryption() {
        for (Encryption encryption : List.of(RSA, AES, NO)) {
            try {
                IGenerator generator = EncryptionFactory.getGenerator(encryption);
                IDecryptor decryptor = EncryptionFactory.getDecryptor(encryption);
                IEncryptor encryptor = EncryptionFactory.getEncryptor(encryption);

                IKeyStorage keyStorage = generator.generateKeyStorage();

                String originalString = "Hello, World!";
                byte[] originalData = originalString.getBytes(US_ASCII);

                byte[] encryptedString = encryptor.encrypt(originalString, keyStorage);
                String decryptedString = decryptor.decrypt(encryptedString, keyStorage);
                assertEquals(originalString, decryptedString,
                        "Failure data changed when it encrypted and decrypted with " + encryption);

                byte[] encryptedData = encryptor.encryptBytes(originalData, keyStorage);
                if (encryption != NO)
                    assertFalse(Arrays.equals(originalData, encryptedData));
                byte[] decryptedData = decryptor.decryptBytes(encryptedData, keyStorage);
                assertArrayEquals(originalData, decryptedData, "Failure data changed when it encrypted and decrypted with " + encryption);
            } catch (Exception e) {
                fail("Exception thrown during encryption/decryption: " + e.getMessage());
            }
        }
    }
}
