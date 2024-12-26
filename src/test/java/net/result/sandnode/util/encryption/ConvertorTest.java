package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConvertorTest {
    @Test
    public void convertTest() throws CreatingKeyException {
        for (IAsymmetricEncryption encryption : EncryptionManager.getAsymmetric()) {
            IAsymmetricKeyStorage keyStorage = encryption.generate();

            try {
                String original = keyStorage.encodedPublicKey();

                IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
                IAsymmetricKeyStorage keyStorage1 = convertor.toKeyStorage(original);

                String string = keyStorage1.encodedPublicKey();

                Assertions.assertEquals(original, string);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            try {
                String original = keyStorage.encodedPrivateKey();

                IAsymmetricConvertor convertor = encryption.privateKeyConvertor();
                IAsymmetricKeyStorage keyStorage1 = convertor.toKeyStorage(original);

                String string = keyStorage1.encodedPrivateKey();

                Assertions.assertEquals(original, string);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }
        }
    }
}
