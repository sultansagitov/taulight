package net.result.sandnode.util.encryption;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IGenerator;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConvertorTest {

    @Test
    public void convertTest() throws CreatingKeyException {
        for (IAsymmetricEncryption encryption : Encryptions.getAsymmetric()) {

            IGenerator generator = encryption.generator();
            IKeyStorage keyStorage = generator.generate();

            {
                IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
                String original = convertor.toEncodedString(keyStorage);

                IKeyStorage keyStorage1 = convertor.toKeyStorage(original);
                String string = convertor.toEncodedString(keyStorage1);

                Assertions.assertEquals(original, string);
            }

            {
                IAsymmetricConvertor convertor = encryption.privateKeyConvertor();
                String original = convertor.toEncodedString(keyStorage);

                IKeyStorage keyStorage1 = convertor.toKeyStorage(original);
                String string = convertor.toEncodedString(keyStorage1);

                Assertions.assertEquals(original, string);
            }
        }
    }

}
