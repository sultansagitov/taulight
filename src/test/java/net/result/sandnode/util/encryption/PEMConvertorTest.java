package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPrivateKeyConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPublicKeyConvertor;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class PEMConvertorTest {

    @Test
    public void PEMConvertTest() throws ReadingKeyException, CreatingKeyException {
        for (Encryption encryption : List.of(RSA)) {

            IGenerator generator = EncryptionFactory.getGenerator(encryption);
            IKeyStorage keyStore = generator.generateKeyStorage();

            {
                RSAPublicKeyConvertor convertor = RSAPublicKeyConvertor.getInstance();
                String originalPEM = convertor.toPEM(keyStore);

                IKeyStorage keyStore1 = convertor.toKeyStorage(originalPEM);
                String pem = convertor.toPEM(keyStore1);

                Assertions.assertEquals(originalPEM, pem);
            }

            {
                RSAPrivateKeyConvertor convertor = RSAPrivateKeyConvertor.getInstance();
                String originalPEM = convertor.toPEM(keyStore);

                IKeyStorage keyStore1 = convertor.toKeyStorage(originalPEM);
                String pem = convertor.toPEM(keyStore1);

                Assertions.assertEquals(originalPEM, pem);
            }
        }
    }

}
