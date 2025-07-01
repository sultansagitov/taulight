package net.result.sandnode.message;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.message.util.MessageTypes;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    @Test
    void toByteArray() throws Exception {

        EncryptionManager.registerAll();

        Headers headers = new Headers()
                .setType(MessageTypes.LOGIN)
                .setConnection(Connection.AGENT2HUB)
                .setBodyEncryption(AsymmetricEncryptions.ECIES)
                .setValue("keyName", "valueData");

        byte[] originalBody = "Hello World!".getBytes();

        RawMessage node1Message = new RawMessage(headers);
        node1Message.setHeadersEncryption(AsymmetricEncryptions.ECIES);
        node1Message.setBody(originalBody);

        KeyStorage keyStorage = AsymmetricEncryptions.ECIES.generate();
        KeyStorageRegistry keyStorageRegistry = new KeyStorageRegistry(keyStorage);

        byte[] byteArray = node1Message.toByteArray(keyStorageRegistry);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);

        EncryptedMessage encrypted = EncryptedMessage.readMessage(in);
        Message node2Message = BaseMessage.decryptMessage(encrypted, keyStorageRegistry);

        // headers
        assertEquals(node1Message.headers().connection(), node2Message.headers().connection());
        assertEquals(node1Message.headers().type(), node2Message.headers().type());
        assertEquals(node1Message.headers().bodyEncryption(), node2Message.headers().bodyEncryption());
        assertEquals(node1Message.headers().getValue("keyName"), node2Message.headers().getValue("keyName"));
        assertEquals(node1Message.headersEncryption(), node2Message.headersEncryption());

        // body
        assertArrayEquals(node1Message.getBody(), node2Message.getBody());
    }
}