package net.result.sandnode;

import net.result.sandnode.client.Client;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.rsa.RSAGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        int port = getPort();

        GlobalKeyStorage serverKeyStorage = getServerKeyStorage();

        ServerThread serverThread = new ServerThread(serverKeyStorage, port);
        serverThread.start();

        Thread.sleep(1000);

        ClientThread clientThread = new ClientThread(port, false);
        clientThread.start();

        Thread.sleep(1000);



        Session session = serverThread.server.sessionList.get(0);

        RawMessage node1Message = getMessage(NO);

        // Sending server to client
        session.sendMessage(node1Message);
        LOGGER.info("Message sent");

        // Receiving client from server
        IMessage node2Message = clientThread.client.receiveMessage();
        LOGGER.info("Message received");

        messagesTest(node1Message, node2Message);



        clientThread.client.close();
        LOGGER.info("Client closed");
        serverThread.server.close();
        LOGGER.info("Server closed");
    }

    public static @NotNull GlobalKeyStorage getServerKeyStorage() {
        RSAGenerator rsaGenerator = RSAGenerator.getInstance();
        RSAKeyStorage rsaKeyStorage = rsaGenerator.generateKeyStorage();

        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage();
        serverKeyStorage.setRSAKeyStorage(rsaKeyStorage);
        return serverKeyStorage;
    }

    public static int getPort() {
        int port = 10240 + new Random().nextInt() % 5000;
        LOGGER.info("Random port: {}", port);
        return port;
    }

    public static @NotNull RawMessage getMessage(Encryption encryption) {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(HAPPY)
                .set(CLIENT2SERVER)
                .set(encryption)
                .set("application/json")
                .set("keyname", "valuedata");

        byte[] originalBody = "Hello World!".getBytes();

        return new RawMessage(headersBuilder, originalBody);
    }

    public static void messagesTest(
            @Nullable IMessage message1,
            @Nullable IMessage message2
    ) throws ReadingKeyException, EncryptionException {
        assertNotNull(message1);
        assertNotNull(message2);
        // headers
        assertEquals(message1.getContentType(), message2.getContentType());
        assertEquals(message1.getConnection(), message2.getConnection());
        assertEquals(message1.getType(), message2.getType());
        assertEquals(message1.getEncryption(), message2.getEncryption());
        assertEquals(message1.getHeaders().get("keyname"), message2.getHeaders().get("keyname"));
        // body
        assertArrayEquals(message1.getBody(), message2.getBody());
    }

    public static class ServerThread extends Thread {

        public final SandnodeServer server;
        private final int port;

        public ServerThread(GlobalKeyStorage serverKeyStorage, int port) {
            setName("Server-thread");
            this.server = new SandnodeServer(serverKeyStorage);
            this.port = port;
        }

        @Override
        public void run() {
            try {
                server.start(port);
                server.acceptSessions();
            } catch (IOException e) {
                LOGGER.error("I/O error", e);
            }
        }
    }

    public static class ClientThread extends Thread {
        public Client client;
        private final int port;
        private final boolean sendKeys;

        public ClientThread(int port, boolean sendKeys) {
            this.sendKeys = sendKeys;
            setName("Client-thread");
            this.port = port;
        }

        @Override
        public void run() {
            GlobalKeyStorage clientKeyStorage = new GlobalKeyStorage();
            client = new Client("localhost", port, clientKeyStorage);
            client.connect();
            if (sendKeys) {
                try {
                    client.getKeys();
                } catch (EncryptionException | IOException | NoSuchEncryptionException | ReadingKeyException |
                         CreatingKeyException | CannotUseEncryption | NoSuchAlgorithmException | DecryptionException |
                         NoSuchReqHandler e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
