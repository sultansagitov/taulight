package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ConvertorTest {
    public static Stream<AsymmetricKeyStorage> hashersProvider() {
        return EncryptionManager.getAsymmetric().stream().map(AsymmetricEncryption::generate);
    }

    @BeforeAll
    static void setUp() {
        EncryptionManager.registerAll();
    }

    @ParameterizedTest
    @MethodSource("hashersProvider")
    public void convertPublicTest(AsymmetricKeyStorage keyStorage) throws CreatingKeyException, CannotUseEncryption {
        String original = keyStorage.encodedPublicKey();

        AsymmetricConvertor convertor = keyStorage.encryption().publicKeyConvertor();
        AsymmetricKeyStorage keyStorage1 = convertor.toKeyStorage(original);

        String string = keyStorage1.encodedPublicKey();

        Assertions.assertEquals(original, string);
    }

    @ParameterizedTest
    @MethodSource("hashersProvider")
    public void convertPrivateTest(AsymmetricKeyStorage keyStorage) throws CreatingKeyException, CannotUseEncryption {
        String original = keyStorage.encodedPrivateKey();

        AsymmetricConvertor convertor = keyStorage.encryption().privateKeyConvertor();
        AsymmetricKeyStorage keyStorage1 = convertor.toKeyStorage(original);

        String string = keyStorage1.encodedPrivateKey();

        Assertions.assertEquals(original, string);
    }
}
