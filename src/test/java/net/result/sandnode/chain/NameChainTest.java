package net.result.sandnode.chain;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.TestAgentConfig;
import net.result.sandnode.chain.sender.NameClientChain;
import net.result.sandnode.config.HubConfigRecord;
import net.result.sandnode.config.ServerConfigRecord;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.exception.ConnectionException;
import net.result.sandnode.exception.InputStreamException;
import net.result.sandnode.exception.OutputStreamException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class NameChainTest {
    private static final String name = "Hub";

    private static final AsymmetricEncryptions a = AsymmetricEncryptions.ECIES;
    private static final ServerChainManager serverCM = new HubServerChainManager() {};
    private static SandnodeClient client;

    private static class TestHub extends Hub {
        public TestHub() {
            super(
                    new KeyStorageRegistry(a.generate()),
                    new HubConfigRecord(name, PasswordHashers.BCRYPT, Path.of("/"))
            );
        }

        @Override
        protected @NotNull ServerChainManager createChainManager() {
            return serverCM;
        }
    }

    private static class TestAgent extends Agent {
        public TestAgent() {
            super(new KeyStorageRegistry(a.generate()), new TestAgentConfig());
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        protected @NotNull ServerChainManager createChainManager() {
            return null;
        }
    }

    @BeforeAll
    public static void setup() throws IOException, OutputStreamException, InputStreamException, ConnectionException {
        EncryptionManager.registerAll();

        FakeSocketPair pair = new FakeSocketPair();

        TestHub node = new TestHub();
        Address serverAddress = new Address("127.0.0.1", 52524);
        ServerConfigRecord serverConfig = new ServerConfigRecord(GlobalTestState.container, serverAddress, a);
        SandnodeServer server = new SandnodeServer(node, serverConfig);
        KeyStorageRegistry ksr = new KeyStorageRegistry(AsymmetricEncryptions.ECIES.generate());
        IOController io = new IOController(pair.socket1, Connection.HUB2AGENT, ksr, serverCM);
        Session session = new Session(server, io);
        serverCM.setSession(session);

        Address clientAddress = new Address("127.0.0.1", 5252);
        //noinspection DataFlowIssue
        client = new SandnodeClient(clientAddress, new TestAgent(), NodeType.HUB, () -> null);
        BaseClientChainManager clientChainManager = new BaseClientChainManager(client) {};
        client.start(clientChainManager, pair.socket2);
    }

    @Test
    void getName() {
        try {
            NameClientChain chain = new NameClientChain(client);
            client.io.chainManager.linkChain(chain);
            String newName = chain.getName();
            client.io.chainManager.removeChain(chain);

            assertEquals(name, newName);
        } catch (Exception e) {
            fail(e);
        }
    }
}
