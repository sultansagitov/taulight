package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.encryption.Encryption.NONE;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptionTest {

    @Test
    public void testEncryptionDecryption() {
        for (IEncryption encryption : Encryptions.list) {
            try {
                IGenerator generator = encryption.generator();
                IEncryptor encryptor = encryption.encryptor();
                IDecryptor decryptor = encryption.decryptor();

                IKeyStorage keyStorage = generator.generate();

                String originalString = "Hello, World!";
                byte[] originalData = originalString.getBytes(US_ASCII);

                byte[] encryptedString = encryptor.encrypt(originalString, keyStorage);
                String decryptedString = decryptor.decrypt(encryptedString, keyStorage);
                assertEquals(originalString, decryptedString,
                        "Failure data changed when it encrypted and decrypted with " + encryption);

                byte[] encryptedData = encryptor.encryptBytes(originalData, keyStorage);
                if (encryption != NONE)
                    assertFalse(Arrays.equals(originalData, encryptedData));
                byte[] decryptedData = decryptor.decryptBytes(encryptedData, keyStorage);
                assertArrayEquals(originalData, decryptedData,
                        "Failure data changed when it encrypted and decrypted with " + encryption);
            } catch (Exception e) {
                fail("Exception thrown during encryption/decryption: " + e.getMessage());
            }
        }
    }
}
