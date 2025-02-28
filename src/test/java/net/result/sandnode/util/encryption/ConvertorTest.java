package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConvertorTest {
    @Test
    public void convertTest() throws CreatingKeyException {
        for (AsymmetricEncryption encryption : EncryptionManager.getAsymmetric()) {
            AsymmetricKeyStorage keyStorage = encryption.generate();

            try {
                String original = keyStorage.encodedPublicKey();

                AsymmetricConvertor convertor = encryption.publicKeyConvertor();
                AsymmetricKeyStorage keyStorage1 = convertor.toKeyStorage(original);

                String string = keyStorage1.encodedPublicKey();

                Assertions.assertEquals(original, string);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            try {
                String original = keyStorage.encodedPrivateKey();

                AsymmetricConvertor convertor = encryption.privateKeyConvertor();
                AsymmetricKeyStorage keyStorage1 = convertor.toKeyStorage(original);

                String string = keyStorage1.encodedPrivateKey();

                Assertions.assertEquals(original, string);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }
        }
    }
}
