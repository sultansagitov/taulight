package net.result.sandnode.messages;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.result.sandnode.messages.util.Connection.HUB2USER;
import static net.result.sandnode.messages.util.MessageType.MSG;
import static net.result.sandnode.util.encryption.Encryption.RSA;
import static org.junit.jupiter.api.Assertions.*;

class HeadersTest {

    @Test
    public void test() throws IOException, NoSuchEncryptionException, NoSuchReqHandler {
        Headers headers = new HeadersBuilder()
                .set(HUB2USER)
                .set(MSG)
                .set(RSA)
                .set("text/html")
                .set("key", "value")
                .build();

        byte[] byteArray = headers.toByteArray();
        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);

        Headers reconstructedHeaders = Headers.getFromBytes(byteArray).build();

        assertEquals(headers.getContentType(), reconstructedHeaders.getContentType());
        assertEquals(headers.getConnection(), reconstructedHeaders.getConnection());
        assertEquals(headers.getType(), reconstructedHeaders.getType());
        assertEquals(headers.getEncryption(), reconstructedHeaders.getEncryption());
        assertEquals(headers.get("key"), reconstructedHeaders.get("key"));
    }
}
