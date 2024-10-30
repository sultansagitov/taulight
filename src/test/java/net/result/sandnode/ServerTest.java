package net.result.sandnode;

import net.result.openhelo.HeloHub;
import net.result.openhelo.HeloUser;
import net.result.sandnode.client.Client;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageType.MSG;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.Encryption.AES;
import static net.result.sandnode.util.encryption.Encryption.RSA;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);

    public static @NotNull GlobalKeyStorage getGlobalRSAKeyStorage() {
        IKeyStorage rsaKeyStorage = RSA.generator().generateKeyStorage();
        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage();
        serverKeyStorage.set(RSA, rsaKeyStorage);
        return serverKeyStorage;
    }

    public static int getPort() {
        int port = 10240 + new Random().nextInt() % 5000;
        LOGGER.info("Random port: {}", port);
        return port;
    }

    public static @NotNull RawMessage getMessage() {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(MSG)
                .set(USER2HUB)
                .set(AES)
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
        assertEquals(message1.getHeaders().getContentType(), message2.getHeaders().getContentType());
        assertEquals(message1.getHeaders().getConnection(), message2.getHeaders().getConnection());
        assertEquals(message1.getHeaders().getType(), message2.getHeaders().getType());
        assertEquals(message1.getHeaders().getEncryption(), message2.getHeaders().getEncryption());
        assertEquals(message1.getHeaders().get("keyname"), message2.getHeaders().get("keyname"));
        // body
        assertArrayEquals(message1.getBody(), message2.getBody());
    }

    @Test
    public void test() throws IOException, ReadingKeyException, EncryptionException, InterruptedException,
            NoSuchEncryptionException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        int port = ServerTest.getPort();

        GlobalKeyStorage serverKeyStorage = ServerTest.getGlobalRSAKeyStorage();

        ServerTest.ServerThread serverThread = new ServerTest.ServerThread(serverKeyStorage, port);
        serverThread.start();

        Thread.sleep(1000);

        ServerTest.ClientThread clientThread = new ServerTest.ClientThread(port);
        clientThread.start();

        Thread.sleep(1000);

        Session session = serverThread.hub.userSessionList.get(0);

        RawMessage node1Message = ServerTest.getMessage();

        // Sending server to client
        session.sendMessage(node1Message);
        LOGGER.info("Message sent");

        // Receiving client from server
        IMessage node2Message = clientThread.client.receiveMessage();
        LOGGER.info("Message received");

        ServerTest.messagesTest(node1Message, node2Message);

        clientThread.client.close();
        LOGGER.info("Client closed");
        serverThread.server.close();
        LOGGER.info("Server closed");
    }

    public static class ServerThread extends Thread {
        public final SandnodeServer server;
        private final int port;
        private final Hub hub;

        public ServerThread(GlobalKeyStorage serverKeyStorage, int port) {
            setName("Server-thread");
            this.port = port;
            hub = new HeloHub(serverKeyStorage);

            ServerConfig serverConfig = null;
            try {
                serverConfig = new ServerConfig(Inet4Address.getByName("localhost"));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            server = new SandnodeServer(hub, serverConfig);
        }

        @Override
        public void run() {
            try {
                server.start(port);
                server.acceptSessions();
            } catch (IOException | NoSuchEncryptionException | ReadingKeyException | ExpectedMessageException |
                     DecryptionException | NoSuchReqHandler | WrongNodeUsed e) {
                LOGGER.error("I/O error", e);
                throw new RuntimeException(e);
            }
        }
    }

    public static class ClientThread extends Thread {
        private final int port;
        public Client client;

        public ClientThread(int port) {
            setName("Client-thread");
            this.port = port;
        }

        @Override
        public void run() {
            User user = new HeloUser();

            // Use localhost for client connection
            client = new Client("localhost", port, user, HUB);

            client.connect();
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
