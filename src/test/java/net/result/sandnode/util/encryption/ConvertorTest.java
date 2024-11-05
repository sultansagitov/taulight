package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.core.interfaces.IGenerator;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class ConvertorTest {

    @Test
    public void convertTest() throws ReadingKeyException, CreatingKeyException, CannotUseEncryption {
        for (Encryption encryption : List.of(RSA)) {

            IGenerator generator = encryption.generator();
            IKeyStorage keyStorage = generator.generateKeyStorage();

            {
                IAsymmetricConvertor convertor = Asymmetric.getPublicConvertor(encryption);
                String original = convertor.toEncodedString(keyStorage);

                IKeyStorage keyStorage1 = convertor.toKeyStorage(original);
                String string = convertor.toEncodedString(keyStorage1);

                Assertions.assertEquals(original, string);
            }

            {
                IAsymmetricConvertor convertor = Asymmetric.getPrivateConvertor(encryption);
                String original = convertor.toEncodedString(keyStorage);

                IKeyStorage keyStorage1 = convertor.toKeyStorage(original);
                String string = convertor.toEncodedString(keyStorage1);

                Assertions.assertEquals(original, string);
            }
        }
    }

}
