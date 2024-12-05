package net.result.sandnode.messages;

import net.result.sandnode.exceptions.MessageSerializationException;
import net.result.sandnode.exceptions.NoSuchMessageTypeException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.util.Headers;
import org.junit.jupiter.api.Test;

import static net.result.sandnode.messages.util.Connection.HUB2AGENT;
import static net.result.sandnode.messages.util.MessageTypes.LOGIN;
import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
import static org.junit.jupiter.api.Assertions.*;

class HeadersTest {

    @Test
    public void test() throws NoSuchEncryptionException, NoSuchMessageTypeException, MessageSerializationException {
        Headers headers = new Headers()
                .set(HUB2AGENT)
                .set(LOGIN)
                .set(RSA)
                .set("key", "value");

        byte[] byteArray = headers.toByteArray();
        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);

        Headers reconstructedHeaders = Headers.getFromBytes(byteArray);

        assertEquals(headers.getConnection(), reconstructedHeaders.getConnection());
        assertEquals(headers.getType(), reconstructedHeaders.getType());
        assertEquals(headers.getBodyEncryption(), reconstructedHeaders.getBodyEncryption());
        assertEquals(headers.get("key"), reconstructedHeaders.get("key"));
    }
}
