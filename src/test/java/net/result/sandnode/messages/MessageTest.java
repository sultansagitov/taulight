package net.result.sandnode.messages;

import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.UnexpectedSocketDisconnect;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageTypes.MSG;
import static net.result.sandnode.util.encryption.AsymmetricEncryption.RSA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {

    @Test
    void toByteArray() throws ReadingKeyException, EncryptionException, IOException, NoSuchEncryptionException,
            DecryptionException, NoSuchReqHandler, KeyStorageNotFoundException, UnexpectedSocketDisconnect {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(MSG)
                .set(USER2HUB)
                .set(RSA)
                .set("keyname", "valuedata");

        byte[] originalBody = "Hello World!".getBytes();

        RawMessage node1Message = new RawMessage(headersBuilder);
        node1Message.setBody(originalBody);


        IKeyStorage keyStorage = RSA.generator().generate();
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage(keyStorage);

        byte[] byteArray = node1Message.toByteArray(globalKeyStorage, RSA);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);

        Message.EncryptedMessage encrypted = Message.readMessage(in);
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