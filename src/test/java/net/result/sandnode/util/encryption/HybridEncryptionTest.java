package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class HybridEncryptionTest {

    static Stream<Object[]> encryptionPairs() {
        return EncryptionManager.getAsymmetric().stream().flatMap(a ->
                EncryptionManager.getSymmetric().stream().map(s -> new Encryption[]{a, s})
        );
    }

    @ParameterizedTest
    @MethodSource("encryptionPairs")
    void testHybridEncryption(AsymmetricEncryption a, SymmetricEncryption s) {
        AsymmetricKeyStorage asymmetricKeyStorage = a.generate();
        SymmetricKeyStorage symmetricKeyStorage = s.generate();

        byte[] bytes = symmetricKeyStorage.toBytes();

        byte[] encryptedBytes;
        byte[] decryptedBytes;
        encryptedBytes = a.encryptBytes(bytes, asymmetricKeyStorage);
        decryptedBytes = a.decryptBytes(encryptedBytes, asymmetricKeyStorage);

        Assertions.assertFalse(Arrays.equals(bytes, encryptedBytes));
        Assertions.assertArrayEquals(bytes, decryptedBytes);

        SymmetricKeyStorage secondKeyStorage = s.toKeyStorage(decryptedBytes);

        String data = "HelloString";
        byte[] encryptedData = s.encrypt(data, secondKeyStorage);
        String decryptedData = s.decrypt(encryptedData, symmetricKeyStorage);
        Assertions.assertEquals(data, decryptedData);
    }
}
