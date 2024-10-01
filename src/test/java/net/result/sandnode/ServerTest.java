package net.result.sandnode;

import net.result.sandnode.client.Client;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.rsa.RSAGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.HAPPY;
import static net.result.sandnode.util.encryption.Encryption.NO;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);

    @Test
    public void test() throws IOException, ReadingKeyException, EncryptionException, InterruptedException, NoSuchEncryptionException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        int port = 10240 + new Random().nextInt() % 5000;
        LOGGER.info("Random port: {}", port);

        RSAGenerator rsaGenerator = RSAGenerator.getInstance();
        RSAKeyStorage rsaKeyStorage = rsaGenerator.generateKeyStorage();

        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage();
        serverKeyStorage.setRSAKeyStorage(rsaKeyStorage);

        SandnodeServer server = new SandnodeServer(serverKeyStorage);


        Thread serverThread = new Thread(() -> {
            try {
                server.start(port);
                server.acceptSessions();
            } catch (IOException e) {
                LOGGER.error("I/O error", e);
            }
        });

        serverThread.setName("Server-thread");
        serverThread.start();


        Thread.sleep(1000);


        var ref = new Object() {
            Client client;
        };
        Thread clientThread = new Thread(() -> {
            GlobalKeyStorage clientKeyStorage = new GlobalKeyStorage();
            ref.client = new Client("localhost", port, clientKeyStorage);
            ref.client.connect();
        });

        clientThread.setName("Client-thread");
        clientThread.start();


        Thread.sleep(2000);


        Session session = server.sessionList.get(0);

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(HAPPY)
                .set(CLIENT2SERVER)
                .set(NO)
                .set("application/json")
                .set("keyname", "valuedata");

        byte[] originalBody = "Hello World!".getBytes();

        RawMessage node1Message = new RawMessage(headersBuilder);
        node1Message.setBody(originalBody);


        // Sending server to client
        session.sendMessage(node1Message);
        LOGGER.info("Message sent");


        // Receiving client from server
        IMessage node2Message = ref.client.receiveMessage();
        LOGGER.info("Message received");


        // headers
        assertNotNull(node2Message);
        assertEquals(node1Message.getContentType(), node2Message.getContentType());
        assertEquals(node1Message.getConnection(), node2Message.getConnection());
        assertEquals(node1Message.getType(), node2Message.getType());
        assertEquals(node1Message.getEncryption(), node2Message.getEncryption());
        assertEquals(node1Message.getHeaders().get("keyname"), node2Message.getHeaders().get("keyname"));

        // body
        assertArrayEquals(node1Message.getBody(), node2Message.getBody());

        ref.client.close();
        LOGGER.info("Client closed");
        server.close();
        LOGGER.info("Server closed");
    }
}
