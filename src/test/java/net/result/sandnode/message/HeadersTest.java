package net.result.sandnode.message;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeadersTest {

    @Test
    public void test() throws SandnodeException {
        EncryptionManager.registerAll();

        Headers headers = new Headers()
                .setConnection(Connection.HUB2AGENT)
                .setType(MessageTypes.LOGIN)
                .setBodyEncryption(AsymmetricEncryptions.ECIES)
                .setValue("key", "value")
                .setValue("empty", "")
                .setValue("", "empty");

        byte[] byteArray = headers.toByteArray();
        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);

        Headers reconstructedHeaders = Headers.fromBytes(byteArray);

        assertEquals(headers.connection(), reconstructedHeaders.connection());
        assertEquals(headers.type(), reconstructedHeaders.type());
        assertEquals(headers.bodyEncryption(), reconstructedHeaders.bodyEncryption());
        assertEquals(headers.getValue("key"), reconstructedHeaders.getValue("key"));
    }
}
