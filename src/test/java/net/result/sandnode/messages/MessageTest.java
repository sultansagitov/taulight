package net.result.sandnode.messages;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static net.result.sandnode.messages.util.Connection.AGENT2HUB;
import static net.result.sandnode.messages.util.MessageTypes.LOGIN;
import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {

    @Test
    void toByteArray() throws EncryptionException, NoSuchEncryptionException, DecryptionException, NoSuchMessageTypeException,
            KeyStorageNotFoundException, UnexpectedSocketDisconnectException, IllegalMessageLengthException,
            MessageSerializationException {
        Headers headers = new Headers()
                .set(LOGIN)
                .set(AGENT2HUB)
                .set(RSA)
                .set("keyname", "valuedata");

        byte[] originalBody = "Hello World!".getBytes();

        RawMessage node1Message = new RawMessage(headers);
        node1Message.setBody(originalBody);


        IKeyStorage keyStorage = RSA.generator().generate();
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage(keyStorage);

        byte[] byteArray = node1Message.toByteArray(globalKeyStorage, RSA);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);

        EncryptedMessage encrypted = EncryptedMessage.readMessage(in);
        IMessage node2Message = Message.decryptMessage(globalKeyStorage, encrypted);

        // headers
        assertEquals(node1Message.getHeaders().getConnection(), node2Message.getHeaders().getConnection());
        assertEquals(node1Message.getHeaders().getType(), node2Message.getHeaders().getType());
        assertEquals(node1Message.getHeaders().getBodyEncryption(), node2Message.getHeaders().getBodyEncryption());
        assertEquals(node1Message.getHeaders().get("keyname"), node2Message.getHeaders().get("keyname"));

        // body
        assertArrayEquals(node1Message.getBody(), node2Message.getBody());
    }
}