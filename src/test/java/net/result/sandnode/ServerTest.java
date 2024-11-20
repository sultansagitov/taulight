package net.result.sandnode;

import net.result.openhelo.HeloHub;
import net.result.sandnode.client.Client;
import net.result.sandnode.config.HubPropertiesConfig;
import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.config.ServerPropertiesConfig;
import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageTypes.MSG;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.util.encryption.SymmetricEncryption.AES;
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
        assertEquals(message1.getHeaders().getConnection(), message2.getHeaders().getConnection());
        assertEquals(message1.getHeaders().getType(), message2.getHeaders().getType());
        assertEquals(message1.getHeaders().getBodyEncryption(), message2.getHeaders().getBodyEncryption());
        assertEquals(message1.getHeaders().get("keyname"), message2.getHeaders().get("keyname"));
        // body
        assertArrayEquals(message1.getBody(), message2.getBody());
    }

    @Test
    public void test() throws Exception {
        int port = ServerTest.getPort();

        IKeyStorage rsaKeyStorage = RSA.generator().generate();
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

        public ServerThread(GlobalKeyStorage serverKeyStorage, int port) throws NoSuchEncryptionException,
                ConfigurationException, CannotUseEncryption, IOException, ReadingKeyException {
            setName("Server-thread");
            this.port = port;
            IHubConfig hubConfig = new HubPropertiesConfig();
            hub = new HeloHub(serverKeyStorage, hubConfig);

            IServerConfig serverConfig = new ServerPropertiesConfig(new Endpoint("localhost", port));

            server = new SandnodeServer(hub, serverConfig);
        }

        @Override
        public void run() {
            try {
                server.start(port);
                server.acceptSessions();
            } catch (Exception e) {
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
            User user;
            try {
                user = new User() {
                    @Override
                    public void onUserMessage(@NotNull IMessage request, @NotNull Session session) {
                    }
                };

                Endpoint endpoint = new Endpoint("localhost", port);
                client = new Client(endpoint, user, HUB);

                client.connect();
                client.getPublicKeyFromServer();
                client.sendSymmetricKey();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
