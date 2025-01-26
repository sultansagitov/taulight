package net.result.sandnode;

import net.result.main.chain.ConsoleClientChainManager;
import net.result.sandnode.config.IClientConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.ServerStartException;
import net.result.sandnode.exception.SocketAcceptException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.encryption.AsymmetricEncryption;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypeManager;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.chain.server.ServerChainManager;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.InMemoryDatabase;
import net.result.sandnode.group.HashSetGroupManager;
import net.result.sandnode.tokens.JWTConfig;
import net.result.sandnode.tokens.JWTTokenizer;
import net.result.taulight.TauAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static net.result.sandnode.encryption.AsymmetricEncryption.ECIES;
import static net.result.sandnode.message.util.Connection.AGENT2HUB;
import static net.result.sandnode.message.util.NodeType.HUB;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);
    private static final int PORT_OFFSET = 10240;
    private static final int PORT_RANGE = 5000;

    private static final AsymmetricEncryption asymmetricEncryption = ECIES;
    private static final SymmetricEncryption symmetricEncryption = AES;
    private static final short CHAIN_ID = (short) ((0xAB << 8) + 0xCD);

    private enum Testing implements MessageType {
        TESTING {
            @Override
            public int asByte() {
                return 100;
            }
        }
    }

    @Test
    public void testMessageTransmission() throws Exception {
        EncryptionManager.registerAll();

        int port = PORT_OFFSET + new Random().nextInt(PORT_RANGE);
        MessageTypeManager.instance().add(Testing.TESTING);
        LOGGER.info("Generated random port: {}", port);

        // Server setup
        IKeyStorage rsaKeyStorage = asymmetricEncryption.generate();
        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage(rsaKeyStorage);
        HubThread hubThread = new HubThread(serverKeyStorage, port);
        hubThread.start();

        TestServerChain.lock.lock();
        TestServerChain.condition = TestServerChain.lock.newCondition();

        // Client setup
        AgentThread agentThread = new AgentThread(port);
        agentThread.start();

        try {
            assertTrue(TestServerChain.condition.await(10, TimeUnit.SECONDS));
        } catch (IllegalMonitorStateException ignored) {}

        // Cleanup
        agentThread.client.close();
        LOGGER.info("Client closed.");
        hubThread.server.close();
        LOGGER.info("Server closed.");
    }

    private static Headers prepareHeaders() {
        return new Headers()
                .setType(Testing.TESTING)
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
        assertEquals(sentMessage.getHeaders().getConnection(), receivedMessage.getHeaders().getConnection());
        assertEquals(sentMessage.getHeaders().getType(), receivedMessage.getHeaders().getType());
        assertEquals(sentMessage.getHeaders().getBodyEncryption(), receivedMessage.getHeaders().getBodyEncryption());
        assertEquals(sentMessage.getHeaders().getValue("keyName"), receivedMessage.getHeaders().getValue("keyName"));
        assertEquals(sentMessage.getHeaders().isFin(), receivedMessage.getHeaders().isFin());
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
            hub = new Hub(serverKeyStorage) {
                @Override
                public @NotNull ServerChainManager createChainManager() {
                    return new TestingBSTServerChainManager();
                }

                @Override
                protected void addAsAgent(Session session) {
                    super.addAsAgent(session);
                }
            };

            ServerConfig serverConfig = new ServerConfig(
                    new Endpoint("localhost", port),
                    null,
                    null,
                    asymmetricEncryption,
                    new HashSetGroupManager(),
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

                CustomClientConfig clientConfig = new CustomClientConfig();
                client = new SandnodeClient(endpoint, agent, HUB, clientConfig);
                client.start(ConsoleClientChainManager::new);
                ClientProtocol.PUB(client.io);
                ClientProtocol.sendSYM(client);

                IMessage sentMessage = prepareMessage();

                IOControl io = client.io;

                TestClientChain testClientChain = new TestClientChain(io, sentMessage);
                io.chainManager.linkChain(testClientChain);
                testClientChain.sync();
                io.chainManager.removeChain(testClientChain);
            } catch (Exception e) {
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
        @Override
        public ServerChain defaultChain(RawMessage ignored) {
            return new TestServerChain(session);
        }
    }

    private static class TestServerChain extends ServerChain {
        public static final Lock lock = new ReentrantLock();
        public static Condition condition;

        public TestServerChain(Session session) {
            super(session);
        }

        @Override
        public void sync() throws InterruptedException {
            IMessage receivedMessage = queue.take();

            // Client sends message via chain
            IMessage sentMessage = prepareMessage();
            sentMessage.getHeaders().setChainID(getID());

            // Validate the message on the server side
            validateMessage(sentMessage, receivedMessage);

            lock.lock();

            try {
                condition.signal();
            } catch (Exception e) {
                LOGGER.error("condition.signal error", e);
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e1) {
                    LOGGER.error("lock.unlock error", e1);
                }
            }
        }
    }

    private static @NotNull IMessage prepareMessage() {
        Headers headers = prepareHeaders();
        IMessage sentMessage = new RawMessage(headers);
        sentMessage.setHeadersEncryption(AES);
        return sentMessage;
    }

    private static class TestClientChain extends ClientChain {
        private final IMessage message;

        public TestClientChain(IOControl io, IMessage message) {
            super(io);
            this.message = message;
        }

        @Override
        public void sync() throws InterruptedException {
            sendFin(message);
        }
    }
}
