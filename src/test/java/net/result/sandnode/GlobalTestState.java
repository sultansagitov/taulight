package net.result.sandnode;

import net.result.sandnode.chain.*;
import net.result.sandnode.config.ServerConfigRecord;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.IOController;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalTestState {
    public static final Container container = new Container();

    public static final String name = "Hub";
    public static SandnodeClient client;
    public static KeyStorageRegistry hubKeyStorage;

    private static boolean init = false;

    public static void initChainTests() throws Exception {
        if (init) return;
        init = true;

        EncryptionManager.registerAll();

        FakeSocketPair pair = new FakeSocketPair();

        HubServerChainManager serverCM = new HubServerChainManager() {};
        hubKeyStorage = new KeyStorageRegistry(AsymmetricEncryptions.ECIES.generate());
        TestHub node = new TestHub(name, serverCM, hubKeyStorage);
        Address serverAddress = new Address("127.0.0.1", 52524);
        ServerConfigRecord serverConfig = new ServerConfigRecord(container, serverAddress, AsymmetricEncryptions.ECIES);
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
}
