package net.result.sandnode;

import net.result.sandnode.chain.*;
import net.result.sandnode.chain.receiver.UnhandledMessageTypeClientChain;
import net.result.sandnode.cluster.HashSetClusterManager;
import net.result.sandnode.config.ClientConfig;
import net.result.sandnode.config.HubConfigRecord;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.config.ServerConfigRecord;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.exception.*;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.*;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.IOController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);

    private static final AsymmetricEncryptions asymmetricEncryption = AsymmetricEncryptions.ECIES;
    private static final SymmetricEncryptions symmetricEncryption = SymmetricEncryptions.AES;
    private static final short CHAIN_ID = (short) ((0xAB << 8) + 0xCD);
    private static final int port = 52524;

    private static final Lock serverStartLock = new ReentrantLock();
    private static final Condition serverStarted = serverStartLock.newCondition();
    private static HubThread hubThread;
    private static AgentThread agentThread;

    public static SandnodeServer server;
    public static Hub hub;

    public static SandnodeClient client;

    private enum Testing implements MessageType {
        TESTING {
            @Override
            public int asByte() {
                return 100;
            }
        }
    }

    @BeforeAll
    public static void setup() {
        EncryptionManager.registerAll();
        MessageTypeManager.instance().add(Testing.TESTING);

        Container container = GlobalTestState.container;

        container.set(HashSetClusterManager.class);

        ServerConfig serverConfig = new ServerConfigRecord(
                container,
                new Address("localhost", port),
                asymmetricEncryption
        );

        KeyStorageRegistry serverKeyStorage = new KeyStorageRegistry(asymmetricEncryption.generate());

        hub = new TestHub(serverKeyStorage);
        server = new SandnodeServer(hub, serverConfig);

        hubThread = new HubThread();
        agentThread = new AgentThread();
    }

    @Test
    public void testMessageTransmission() throws Exception {
        hubThread.start();

        TestServerChain.lock.lock();
        TestServerChain.condition = TestServerChain.lock.newCondition();

        agentThread.start();

        assertTrue(TestServerChain.condition.await(10, TimeUnit.SECONDS));

        // Cleanup
        client.close();
        LOGGER.info("Client closed.");
        server.closeWithoutDBShutdown();
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

    private static @NotNull Message prepareMessage() {
        Headers headers = prepareHeaders();
        Message sentMessage = new RawMessage(headers);
        sentMessage.setHeadersEncryption(SymmetricEncryptions.AES);
        return sentMessage;
    }

    private static void validateMessage(Message sentMessage, Message receivedMessage) throws DeserializationException {
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
        public HubThread() {
            setName("HubThread");
        }

        @Override
        public void run() {
            serverStartLock.lock();
            try {
                server.start(port);
                serverStarted.signal();
            } catch (ServerStartException e) {
                LOGGER.error("Server failed to start or accept sessions.", e);
                fail(e);
                throw new ImpossibleRuntimeException(e);
            } finally {
                serverStartLock.unlock();
            }

            assertThrows(SocketAcceptException.class, server::acceptSessions);
        }
    }

    private static class TestHub extends Hub {
        public TestHub(KeyStorageRegistry serverKeyStorage) {
            super(serverKeyStorage, new HubConfigRecord("Test Hub", null, null));
        }

        @Override
        public @NotNull ServerChainManager createChainManager() {
            return new TestHubServerChainManager();
        }
    }

    private static class TestHubServerChainManager extends HubServerChainManager {
        @Override
        public ServerChain createSessionChain(MessageType type) {
            return type == Testing.TESTING ? new TestServerChain(session) : super.createSessionChain(type);
        }
    }

    private static class TestServerChain extends ServerChain implements ReceiverChain {
        public static final Lock lock = new ReentrantLock();
        public static Condition condition;

        public TestServerChain(Session session) {
            setSession(session);
        }

        @Override
        public @Nullable Message handle(RawMessage receivedMessage) throws DeserializationException {
            // Client sends message via chain
            Message sentMessage = prepareMessage();
            sentMessage.headers().setChainID(getID());

            // Validate the message on the server side
            validateMessage(sentMessage, receivedMessage);

            lock.lock();

            try {
                condition.signal();
            } catch (Exception e) {
                LOGGER.error("condition.signal error", e);
                fail(e);
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e1) {
                    LOGGER.error("lock.unlock error", e1);
                    fail(e1);
                }
            }
            return null;
        }
    }

    public static class AgentThread extends Thread {
        public AgentThread() {
            setName("AgentThread");
        }

        @Override
        public void run() {
            try {
                Agent agent = new TestAgent();
                Address address = new Address("localhost", port);

                TestClientConfig clientConfig = new TestClientConfig();
                client = new SandnodeClient(address, agent, NodeType.HUB, clientConfig);
                ClientChainManager chainManager = new TestClientChainManager(client);
                client.start(chainManager);
                ClientProtocol.PUB(client);
                ClientProtocol.sendSYM(client);

                Message sentMessage = prepareMessage();

                IOController io = client.io();

                TestClientChain chain = new TestClientChain(client);
                io.chainManager.linkChain(chain);
                chain.sendTestMessage(sentMessage);
                io.chainManager.removeChain(chain);
            } catch (Exception e) {
                LOGGER.error("Client encountered an error.", e);
                fail(e);
                throw new RuntimeException(e);
            }
        }
    }

    private static class TestClientChainManager extends BaseClientChainManager {
        public TestClientChainManager(SandnodeClient client) {
            super(client);
        }

        @Override
        public ReceiverChain createChain(MessageType type) {
            return new UnhandledMessageTypeClientChain(client);
        }
    }

    public static class TestAgent extends Agent {
        public TestAgent() {
            super(new KeyStorageRegistry(), new TestAgentConfig());
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public @NotNull ServerChainManager createChainManager() {
            return null;
        }
    }

    public static class TestClientConfig implements ClientConfig {
        @Override
        public @NotNull SymmetricEncryption symmetricKeyEncryption() {
            return symmetricEncryption;
        }
    }

    private static class TestClientChain extends ClientChain {
        public TestClientChain(SandnodeClient client) {
            super(client);
        }

        public void sendTestMessage(Message message) throws InterruptedException, UnprocessedMessagesException {
            sendFin(message);
        }
    }
}
