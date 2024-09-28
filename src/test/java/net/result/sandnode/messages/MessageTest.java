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
import java.security.NoSuchAlgorithmException;

import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.HAPPY;
import static net.result.sandnode.util.encryption.Encryption.RSA;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void toByteArray() throws ReadingKeyException, EncryptionException, IOException, NoSuchEncryptionException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(HAPPY)
                .set(CLIENT2SERVER)
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
        assertEquals(node1Message.getContentType(), node2Message.getContentType());
        assertEquals(node1Message.getConnection(), node2Message.getConnection());
        assertEquals(node1Message.getType(), node2Message.getType());
        assertEquals(node1Message.getEncryption(), node2Message.getEncryption());
        assertEquals(node1Message.getHeaders().get("keyname"), node2Message.getHeaders().get("keyname"));

        // body
        assertArrayEquals(node1Message.getBody(), node2Message.getBody());
    }
}