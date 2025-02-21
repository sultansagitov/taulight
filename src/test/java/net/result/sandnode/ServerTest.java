package net.result.sandnode;

import net.result.main.chain.ConsoleClientChainManager;
import net.result.sandnode.config.ClientConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.config.ServerConfigRecord;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.exception.*;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.*;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.chain.server.ServerChainManager;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.InMemoryDatabase;
import net.result.sandnode.group.HashSetGroupManager;
import net.result.sandnode.tokens.JWTConfig;
import net.result.sandnode.tokens.JWTTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);
    private static final int PORT_OFFSET = 10240;
    private static final int PORT_RANGE = 5000;

    private static final AsymmetricEncryptions asymmetricEncryption = AsymmetricEncryptions.ECIES;
    private static final SymmetricEncryptions symmetricEncryption = SymmetricEncryptions.AES;
    private static final short CHAIN_ID = (short) ((0xAB << 8) + 0xCD);
    private static int port;

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

        port = PORT_OFFSET + new Random().nextInt(PORT_RANGE);
        MessageTypeManager.instance().add(Testing.TESTING);
        LOGGER.info("Generated random port: {}", port);

        // Server setup
        KeyStorage rsaKeyStorage = asymmetricEncryption.generate();
        GlobalKeyStorage serverKeyStorage = new GlobalKeyStorage(rsaKeyStorage);
        HubThread hubThread = new HubThread(serverKeyStorage);
        hubThread.start();

        TestServerChain.lock.lock();
        TestServerChain.condition = TestServerChain.lock.newCondition();

        // Client setup
        AgentThread agentThread = new AgentThread();
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
                .setConnection(Connection.AGENT2HUB)
                .setBodyEncryption(ServerTest.symmetricEncryption)
                .setValue("keyName", "valueData")
                .setValue("keyName1", "valueData")
                .setValue("keyName2", "valueData")
                .setValue("keyName3", "valueData")
                .setFin(true)
                .setChainID(CHAIN_ID);
    }

    private static @NotNull IMessage prepareMessage() {
        Headers headers = prepareHeaders();
        IMessage sentMessage = new RawMessage(headers);
        sentMessage.setHeadersEncryption(SymmetricEncryptions.AES);
        return sentMessage;
    }

    private static void validateMessage(IMessage sentMessage, IMessage receivedMessage) {
        // Validate headers
        assertEquals(sentMessage.headers().connection(), receivedMessage.headers().connection());
        assertEquals(sentMessage.headers().type(), receivedMessage.headers().type());
        assertEquals(sentMessage.headers().bodyEncryption(), receivedMessage.headers().bodyEncryption());
        assertEquals(sentMessage.headers().getValue("keyName"), receivedMessage.headers().getValue("keyName"));
        assertEquals(sentMessage.headers().fin(), receivedMessage.headers().fin());
        assertEquals(sentMessage.headers().chainID(), receivedMessage.headers().chainID());

        // Validate body
        assertArrayEquals(sentMessage.getBody(), receivedMessage.getBody());
    }

    public static class HubThread extends Thread {
        public final SandnodeServer server;
        public final Hub hub;

        public HubThread(GlobalKeyStorage serverKeyStorage) {
            setName("HubThread");
            hub = new TestHub(serverKeyStorage);

            ServerConfig serverConfig = new ServerConfigRecord(
                    new Endpoint("localhost", port),
                    null,
                    null,
                    asymmetricEncryption,
                    new HashSetGroupManager(),
                    new InMemoryDatabase(PasswordHashers.BCRYPT),
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

    private static class TestHub extends Hub {
        public TestHub(GlobalKeyStorage serverKeyStorage) {
            super(serverKeyStorage);
        }

        @Override
        public @NotNull ServerChainManager createChainManager() {
            return new TestingBSTServerChainManager();
        }

        @Override
        protected void addAsAgent(Session session) {
            super.addAsAgent(session);
        }
    }

    private static class TestingBSTServerChainManager extends BSTServerChainManager {
        @Override
        public ServerChain createChain(MessageType type) {
            return type == Testing.TESTING ? new TestServerChain(session) : super.createChain(type);
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
            sentMessage.headers().setChainID(getID());

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

    public static class AgentThread extends Thread {
        public SandnodeClient client;

        public AgentThread() {
            setName("AgentThread");
        }

        @Override
        public void run() {
            try {
                Agent agent = new TestAgent();
                Endpoint endpoint = new Endpoint("localhost", port);

                TestClientConfig clientConfig = new TestClientConfig();
                ConsoleClientChainManager chainManager = new ConsoleClientChainManager();
                client = new SandnodeClient(endpoint, agent, NodeType.HUB, clientConfig);
                client.start(chainManager);
                ClientProtocol.PUB(client.io);
                ClientProtocol.sendSYM(client);

                IMessage sentMessage = prepareMessage();

                IOController io = client.io;

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

    private static class TestAgent extends Agent {
        public TestAgent() {
            super(new GlobalKeyStorage());
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection) {
            return null;
        }
    }

    private static class TestClientConfig implements ClientConfig {
        @Override
        public @NotNull SymmetricEncryption symmetricKeyEncryption() {
            return symmetricEncryption;
        }

        @Override
        public void saveKey(@NotNull Endpoint endpoint, @NotNull AsymmetricKeyStorage keyStorage) {
        }

        @Override
        public Optional<AsymmetricKeyStorage> getPublicKey(@NotNull Endpoint endpoint) {
            return Optional.empty();
        }
    }

    private static class TestClientChain extends ClientChain {
        private final IMessage message;

        public TestClientChain(IOController io, IMessage message) {
            super(io);
            this.message = message;
        }

        @Override
        public void sync() throws InterruptedException {
            sendFin(message);
        }
    }
}
