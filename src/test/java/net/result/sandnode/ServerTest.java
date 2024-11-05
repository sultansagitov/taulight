package net.result.sandnode;

import net.result.openhelo.HeloHub;
import net.result.openhelo.HeloUser;
import net.result.sandnode.client.Client;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
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
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageType.MSG;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.Encryption.AES;
import static net.result.sandnode.util.encryption.Encryption.RSA;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);

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
    ) {
        assertNotNull(message1);
        assertNotNull(message2);
        // headers
        assertEquals(message1.getHeaders().getContentType(), message2.getHeaders().getContentType());
        assertEquals(message1.getHeaders().getConnection(), message2.getHeaders().getConnection());
        assertEquals(message1.getHeaders().getType(), message2.getHeaders().getType());
        assertEquals(message1.getHeaders().getBodyEncryption(), message2.getHeaders().getBodyEncryption());
        assertEquals(message1.getHeaders().get("keyname"), message2.getHeaders().get("keyname"));
        // body
        assertArrayEquals(message1.getBody(), message2.getBody());
    }

    @Test
    public void test() throws IOException, ReadingKeyException, EncryptionException, NoSuchEncryptionException,
            DecryptionException, NoSuchReqHandler {
        int port = ServerTest.getPort();

        IKeyStorage rsaKeyStorage = RSA.generator().generateKeyStorage();
        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage(rsaKeyStorage);

        ServerTest.ServerThread serverThread = new ServerTest.ServerThread(serverKeyStorage, port);
        serverThread.start();

        ServerTest.ClientThread clientThread = new ServerTest.ClientThread(port);
        clientThread.start();

        //noinspection LoopConditionNotUpdatedInsideLoop
        while (serverThread.hub.userSessionList.isEmpty()) Thread.onSpinWait();
        Session session = serverThread.hub.userSessionList.get(0);

        RawMessage node1Message = ServerTest.getMessage();

        // Sending server to client
        while (!session.sessionKeyStorage.has(AES)) Thread.onSpinWait();
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
            HubConfig hubConfig = new HubConfig();
            hub = new HeloHub(serverKeyStorage, hubConfig);

            ServerConfig serverConfig;
            serverConfig = new ServerConfig(new Endpoint("localhost", port));

            server = new SandnodeServer(hub, serverConfig);
        }

        @Override
        public void run() {
            try {
                server.start(port);
            } catch (IOException e) {
                LOGGER.error("I/O error", e);
                throw new RuntimeException(e);
            }
            server.acceptSessions();
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

            Endpoint endpoint = new Endpoint("localhost", port);
            client = new Client(endpoint, user, HUB);

            client.connect();
            try {
                client.getPublicKeyFromServer();
                client.sendSymmetricKey();
            } catch (EncryptionException | IOException | NoSuchEncryptionException | ReadingKeyException |
                     CreatingKeyException | CannotUseEncryption | DecryptionException | NoSuchReqHandler e) {
                throw new RuntimeException(e);
            }
        }
    }
}
