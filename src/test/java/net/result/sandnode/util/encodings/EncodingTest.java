package net.result.sandnode.util.encodings;

import net.result.sandnode.exceptions.NoSuchEncoderException;
import net.result.sandnode.util.encodings.interfaces.IDecoder;
import net.result.sandnode.util.encodings.interfaces.IEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class EncodingTest {
    @Test
    public void testEncoding() throws NoSuchEncoderException {
        for (String enc : List.of("base64", "hex", "no")) {
            IEncoder encoder = EncodingFactory.getEncoder(enc);
            IDecoder decoder = EncodingFactory.getDecoder(enc);

            String originalString = "Hello world";

            // String
            String encodedString = encoder.encode(originalString);
            byte[] decodedString = decoder.decode(encodedString);

            Assertions.assertEquals(originalString, new String(decodedString));

            // Bytes
            byte[] originalData = originalString.getBytes(US_ASCII);
            String encodedData = encoder.encode(originalData);
            if (!enc.equals("no")) {
                Assertions.assertNotEquals(originalString, encodedData);
            }
            byte[] decryptedData = decoder.decode(encodedData);
            Assertions.assertArrayEquals(originalData, decryptedData);
        }
    }
}
