package net.result.sandnode.messages;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.rsa.RSAGenerator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageType.MSG;
import static net.result.sandnode.util.encryption.Encryption.RSA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {

    @Test
    void toByteArray() throws ReadingKeyException, EncryptionException, IOException, NoSuchEncryptionException,
            DecryptionException, NoSuchReqHandler {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(MSG)
                .set(USER2HUB)
                .set(RSA)
                .set("application/json")
                .set("keyname", "valuedata");

        byte[] originalBody = "Hello World!".getBytes();

        RawMessage node1Message = new RawMessage(headersBuilder);
        node1Message.setBody(originalBody);




        RSAGenerator rsaGenerator = RSAGenerator.getInstance();
        RSAKeyStorage rsaKeyStorage = rsaGenerator.generateKeyStorage();

        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        globalKeyStorage.setRSAKeyStorage(rsaKeyStorage);

        byte[] byteArray = node1Message.toByteArray(globalKeyStorage, RSA);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);



        IMessage node2Message = Message.fromInput(in, globalKeyStorage);


        // headers
        assertEquals(node1Message.getHeaders().getContentType(), node2Message.getHeaders().getContentType());
        assertEquals(node1Message.getHeaders().getConnection(), node2Message.getHeaders().getConnection());
        assertEquals(node1Message.getHeaders().getType(), node2Message.getHeaders().getType());
        assertEquals(node1Message.getHeaders().getEncryption(), node2Message.getHeaders().getEncryption());
        assertEquals(node1Message.getHeaders().get("keyname"), node2Message.getHeaders().get("keyname"));

        // body
        assertArrayEquals(node1Message.getBody(), node2Message.getBody());
    }
}