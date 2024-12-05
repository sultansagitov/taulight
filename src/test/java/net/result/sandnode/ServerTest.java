package net.result.sandnode;

import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.config.*;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.*;
import net.result.sandnode.messages.types.ExitMessage;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.server.*;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static net.result.sandnode.messages.util.Connection.AGENT2HUB;
import static net.result.sandnode.messages.util.MessageTypes.*;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);
    private static final int PORT_OFFSET = 10240;
    private static final int PORT_RANGE = 5000;

    @Test
    public void testMessageTransmission() throws Exception {
        int port = PORT_OFFSET + new Random().nextInt(PORT_RANGE);
        LOGGER.info("Generated random port: {}", port);

        // Server setup
        IKeyStorage rsaKeyStorage = RSA.generator().generate();
        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage(rsaKeyStorage);
        ServerThread serverThread = new ServerThread(serverKeyStorage, port);
        serverThread.start();

        // Client setup
        ClientThread clientThread = new ClientThread(port);
        clientThread.start();

        // Wait for server configuration
        waitUntilConfigured(serverThread);

        Session session = serverThread.hub.agentSessionList.get(0);

        // Wait for session key storage
        waitForSessionKey(session);

        // Prepare and send a message
        Headers headers = prepareHeaders();
        byte[] originalBody = "Hello World!".getBytes();
        RawMessage sentMessage = new RawMessage(headers, originalBody);

        session.io.sendMessage(sentMessage);
        LOGGER.info("Message sent from server to client.");

        // Receive and validate the message
        IMessage receivedMessage = clientThread.client.io.receiveMessage();
        LOGGER.info("Message received by client.");

        validateMessage(sentMessage, receivedMessage);

        clientThread.client.io.sendMessage(new ExitMessage(new Headers()));

        // Cleanup
        clientThread.client.close();
        LOGGER.info("Client closed.");
        serverThread.server.close();
        LOGGER.info("Server closed.");
    }

    private static void waitUntilConfigured(@NotNull ServerThread serverThread) {
        while (serverThread.server.isConfiguring()) {
            Thread.onSpinWait();
        }
    }

    private static void waitForSessionKey(@NotNull Session session) {
        while (!session.globalKeyStorage.has(AES)) {
            Thread.onSpinWait();
        }
    }

    private static Headers prepareHeaders() {
        return new Headers()
                .set(PUB)
                .set(AGENT2HUB)
                .set(AES)
                .set("keyname", "valuedata")
                .setFin(true);
    }

    private static void validateMessage(RawMessage sentMessage, IMessage receivedMessage) {
        // Validate headers
        assertEquals(sentMessage.getHeaders().getConnection(), receivedMessage.getHeaders().getConnection());
        assertEquals(sentMessage.getHeaders().getType(), receivedMessage.getHeaders().getType());
        assertEquals(sentMessage.getHeaders().getBodyEncryption(), receivedMessage.getHeaders().getBodyEncryption());
        assertEquals(sentMessage.getHeaders().get("keyname"), receivedMessage.getHeaders().get("keyname"));
        assertEquals(sentMessage.getHeaders().getFin(), receivedMessage.getHeaders().getFin());

        // Validate body
        assertArrayEquals(sentMessage.getBody(), receivedMessage.getBody());
    }

    public static class ServerThread extends Thread {

        public final SandnodeServer server;
        public final Hub hub;
        private final int port;

        public ServerThread(GlobalKeyStorage serverKeyStorage, int port) {
            setName("ServerThread");
            this.port = port;
            IHubConfig hubConfig = new HubConfig(RSA, AES);
            hub = new CustomHub(serverKeyStorage, hubConfig);

            IServerConfig serverConfig = new ServerConfig(new Endpoint("localhost", port), null, null);
            server = new SandnodeServer(hub, serverConfig);
        }

        @Override
        public void run() {
            try {
                server.start(port);
                Assertions.assertThrows(SocketAcceptionException.class, server::acceptSessions);
            } catch (ServerStartException e) {
                throw new RuntimeException("Server failed to start or accept sessions.", e);
            }
        }

        private static class CustomHub extends Hub {
            public CustomHub(GlobalKeyStorage serverKeyStorage, IHubConfig hubConfig) {
                super(serverKeyStorage, hubConfig);
            }

            @Override
            public void onAgentMessage(@NotNull IMessage request, @NotNull Session session) {
            }
        }
    }

    public static class ClientThread extends Thread {

        private final int port;
        public SandnodeClient client;

        public ClientThread(int port) {
            setName("ClientThread");
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Agent agent = new CustomAgent();
                Endpoint endpoint = new Endpoint("localhost", port);

                client = new SandnodeClient(endpoint, agent, HUB, new ClientPropertiesConfig());
                ClientProtocol.PUB(client);
                ClientProtocol.sendSYM(client);

                IMessage req = client.io.receiveMessage();
                ExpectedMessageException.check(req, REQ);

                RegistrationResponse response = AgentProtocol.registrationResponse(client, "myname", "mypassword");
                client.io.sendMessage(response);
            } catch (Exception e) {
                throw new RuntimeException("Client encountered an error.", e);
            }
        }

        private static class CustomAgent extends Agent {
            public CustomAgent() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption,
                    FSException {}

            @Override
            public void onAgentMessage(@NotNull IMessage request, @NotNull Session session) {
            }
        }
    }
}
