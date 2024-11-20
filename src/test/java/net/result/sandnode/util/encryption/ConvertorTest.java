package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.result.sandnode.util.encryption.AsymmetricEncryption.RSA;

public class ConvertorTest {

    @Test
    public void convertTest() throws ReadingKeyException, CreatingKeyException {
        for (IAsymmetricEncryption encryption : List.of(RSA)) {

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
