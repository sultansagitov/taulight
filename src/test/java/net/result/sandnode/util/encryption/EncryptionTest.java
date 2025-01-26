package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.encryption.Encryption.NONE;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptionTest {

    @Test
    public void testEncryptionDecryption() throws Exception {
        for (IEncryption encryption : EncryptionManager.instance().list) {
            KeyStorage keyStorage = encryption.generate();

            String originalString = "Hello, World!";
            byte[] originalData = originalString.getBytes(US_ASCII);

            byte[] encryptedString = encryption.encrypt(originalString, keyStorage);
            String decryptedString = encryption.decrypt(encryptedString, keyStorage);
            assertEquals(originalString, decryptedString,
                    "Failure data changed when it encrypted and decrypted with " + encryption);

            byte[] encryptedData = encryption.encryptBytes(originalData, keyStorage);
            if (encryption != NONE)
                assertFalse(Arrays.equals(originalData, encryptedData));
            byte[] decryptedData = encryption.decryptBytes(encryptedData, keyStorage);
            assertArrayEquals(originalData, decryptedData,
                    "Failure data changed when it encrypted and decrypted with " + encryption);
        }
    }
}
