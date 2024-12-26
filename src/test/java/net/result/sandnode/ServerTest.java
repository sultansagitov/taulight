package net.result.sandnode;

import net.result.main.chains.ConsoleClientChainManager;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.config.*;
import net.result.sandnode.encryption.AsymmetricEncryption;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.*;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.messages.util.MessageTypeManager;
import net.result.sandnode.server.*;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.chain.server.IServerChainManager;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.util.db.InMemoryDatabase;
import net.result.sandnode.util.group.GroupManager;
import net.result.sandnode.util.tokens.JWTConfig;
import net.result.sandnode.util.tokens.JWTTokenizer;
import net.result.taulight.TauAgent;
import net.result.taulight.TauHub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static net.result.sandnode.encryption.AsymmetricEncryption.ECIES;
import static net.result.sandnode.messages.util.Connection.AGENT2HUB;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);
    private static final int PORT_OFFSET = 10240;
    private static final int PORT_RANGE = 5000;

    private static final AsymmetricEncryption asymmetricEncryption = ECIES;
    private static final SymmetricEncryption symmetricEncryption = AES;
    private static final short CHAIN_ID = (short) ((0xAB << 8) + 0xCD);
    private static final IMessageType testingType = new IMessageType() {
        @Override
        public int asByte() {
            return 100;
        }

        @Override
        public String name() {
            return "TESTING";
        }
    };

    @Test
    public void testMessageTransmission() throws Exception {
        EncryptionManager.registerAll();

        int port = PORT_OFFSET + new Random().nextInt(PORT_RANGE);
        MessageTypeManager.instance().add(testingType);
        LOGGER.info("Generated random port: {}", port);

        // Server setup
        IKeyStorage rsaKeyStorage = asymmetricEncryption.generate();
        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage(rsaKeyStorage);
        HubThread hubThread = new HubThread(serverKeyStorage, port);
        hubThread.start();

        // Client setup
        AgentThread agentThread = new AgentThread(port);
        agentThread.start();

        while (hubThread.hub.agentSessionList.isEmpty())
            Thread.onSpinWait();

        // Client sends message via chain
        Headers headers = prepareHeaders();
        EmptyMessage sentMessage = new EmptyMessage(headers);

        IChain testClientChain = new TestClientChain(agentThread.client.io, sentMessage);
        agentThread.client.io.chainManager.addChain(testClientChain);

        testClientChain.sync();

        while (TestServerChain.message == null) {
            Thread.onSpinWait();
        }

        IMessage receivedMessage = TestServerChain.message;

        // Validate the message on the server side
        validateMessage(sentMessage, receivedMessage);

        // Cleanup
        agentThread.client.close();
        LOGGER.info("Client closed.");
        hubThread.server.close();
        LOGGER.info("Server closed.");
    }

    private static Headers prepareHeaders() {
        return new Headers()
                .setType(testingType)
                .setConnection(AGENT2HUB)
                .setBodyEncryption(ServerTest.symmetricEncryption)
                .setValue("keyName", "valueData")
                .setValue("keyName1", "valueData")
                .setValue("keyName2", "valueData")
                .setValue("keyName3", "valueData")
                .setFin(true)
                .setChainID(CHAIN_ID);
    }

    private static void validateMessage(IMessage sentMessage, IMessage receivedMessage) {
        // Validate headers

        LOGGER.debug("{} {}", sentMessage, receivedMessage);

        assertEquals(sentMessage.getHeaders().getConnection(), receivedMessage.getHeaders().getConnection());
        assertEquals(sentMessage.getHeaders().getType(), receivedMessage.getHeaders().getType());
        assertEquals(sentMessage.getHeaders().getBodyEncryption(), receivedMessage.getHeaders().getBodyEncryption());
        assertEquals(sentMessage.getHeaders().getValue("keyName"), receivedMessage.getHeaders().getValue("keyName"));
        assertEquals(sentMessage.getHeaders().getFin(), receivedMessage.getHeaders().getFin());
        assertEquals(sentMessage.getHeaders().getChainID(), receivedMessage.getHeaders().getChainID());

        // Validate body
        assertArrayEquals(sentMessage.getBody(), receivedMessage.getBody());
    }

    public static class HubThread extends Thread {

        public final SandnodeServer server;
        public final Hub hub;
        private final int port;

        public HubThread(GlobalKeyStorage serverKeyStorage, int port) {
            setName("HubThread");
            this.port = port;
            hub = new TauHub(serverKeyStorage) {
                @Override
                public @NotNull IServerChainManager createChainManager() {
                    return new TestingBSTServerChainManager();
                }
            };

            IServerConfig serverConfig = new ServerConfig(
                    new Endpoint("localhost", port),
                    null,
                    null,
                    asymmetricEncryption,
                    new GroupManager(),
                    new InMemoryDatabase(),
                    new JWTTokenizer(new JWTConfig("justTesting"))
            );
            server = new SandnodeServer(hub, serverConfig);
        }

        @Override
        public void run() {
            try {
                server.start(port);
                Assertions.assertThrows(SocketAcceptException.class, server::acceptSessions);
            } catch (ServerStartException e) {
                LOGGER.error("Server failed to start or accept sessions.", e);
                throw new ImpossibleRuntimeException(e);
            }
        }
    }

    public static class AgentThread extends Thread {
        private final int port;
        public SandnodeClient client;

        public AgentThread(int port) {
            setName("AgentThread");
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Agent agent = new TauAgent();
                Endpoint endpoint = new Endpoint("localhost", port);

                client = new SandnodeClient(
                        endpoint,
                        agent,
                        HUB,
                        new CustomClientConfig(),
                        ConsoleClientChainManager::new
                );
                ClientProtocol.PUB(client.io);
                ClientProtocol.sendSYM(client);
            } catch (SandnodeException | InterruptedException e) {
                LOGGER.error("Client encountered an error.", e);
                throw new RuntimeException(e);
            }
        }
    }

    private static class CustomClientConfig implements IClientConfig {
        @Override
        public @NotNull ISymmetricEncryption symmetricKeyEncryption() {
            return symmetricEncryption;
        }

        @Override
        public void saveKey(@NotNull Endpoint endpoint, @NotNull IAsymmetricKeyStorage keyStorage) {
        }

        @Override
        public Optional<IAsymmetricKeyStorage> getPublicKey(@NotNull Endpoint endpoint) {
            return Optional.empty();
        }
    }

    private static class TestingBSTServerChainManager extends BSTServerChainManager {
        private TestingBSTServerChainManager() {
            super();
        }

        @Override
        public ServerChain defaultChain(RawMessage ignored) {
            return new TestServerChain(session);
        }
    }

    private static class TestServerChain extends ServerChain {
        public static IMessage message;

        public TestServerChain(Session session) {
            super(session);
        }

        @Override
        public void start() throws InterruptedException {
            message = queue.take();
            LOGGER.debug("messsssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss {}", message);
        }
    }

    private static class TestClientChain extends ClientChain {
        private final IMessage message;

        public TestClientChain(IOControl io, IMessage message) {
            super(io);
            this.message = message;
        }

        @Override
        public void start() throws InterruptedException {
            send(message);
        }
    }
}
