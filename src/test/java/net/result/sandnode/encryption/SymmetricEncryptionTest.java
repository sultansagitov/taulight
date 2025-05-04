package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class SymmetricEncryptionTest {
    @BeforeAll
    static void setUp() {
        EncryptionManager.registerAll();
    }

    public static Stream<SymmetricKeyStorage> encryptionsProvider() {
        return EncryptionManager.getSymmetric().stream().map(SymmetricEncryption::generate);
    }

    @ParameterizedTest
    @MethodSource("encryptionsProvider")
    public void toBytes(SymmetricKeyStorage keyStorage) {
        byte[] bytes1 = keyStorage.toBytes();
        byte[] bytes2 = keyStorage.encryption().toKeyStorage(bytes1).toBytes();
        Assertions.assertArrayEquals(bytes1, bytes2);
    }
}