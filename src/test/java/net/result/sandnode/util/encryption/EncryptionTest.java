package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.EncryptionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptionTest {

    public static Stream<Encryption> encryptionTest() {
        return EncryptionManager.instance().list.stream();
    }

    @ParameterizedTest
    @MethodSource("encryptionTest")
    void testEncryptionDecryption(Encryption encryption)
            throws EncryptionException, CryptoException, DecryptionException {
        KeyStorage keyStorage = encryption.generate();

        String originalString = "Hello, World!";
        byte[] originalData = originalString.getBytes(StandardCharsets.UTF_8);

        byte[] encryptedString = encryption.encrypt(originalString, keyStorage);
        String decryptedString = encryption.decrypt(encryptedString, keyStorage);
        assertEquals(originalString, decryptedString,
                "Failure data changed when it encrypted and decrypted with " + encryption);

        byte[] encryptedData = encryption.encryptBytes(originalData, keyStorage);
        if (encryption != Encryptions.NONE) assertFalse(Arrays.equals(originalData, encryptedData));
        byte[] decryptedData = encryption.decryptBytes(encryptedData, keyStorage);
        assertArrayEquals(originalData, decryptedData,
                "Failure data changed when it encrypted and decrypted with " + encryption);
    }
}
