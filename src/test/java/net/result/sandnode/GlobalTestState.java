package net.result.sandnode;

import net.result.sandnode.chain.*;
import net.result.sandnode.config.ServerConfigRecord;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Container;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalTestState {
    public static final Container container = new Container();

    public static final String name = "Hub";
    public static KeyStorageRegistry hubKeyStorage;
    public static Session session;
    public static SandnodeClient client;

    private static boolean init = false;

    public static void initChainTests() throws Exception {
        if (init) return;
        init = true;

        EncryptionManager.registerAll();

        var pair = new FakeSocketPair();

        hubKeyStorage = new KeyStorageRegistry(AsymmetricEncryptions.ECIES.generate());

        var serverCM = new HubServerChainManager() {};
        var node = new TestHub(name, serverCM, hubKeyStorage);
        var serverAddress = new Address("127.0.0.1", 52524);
        var serverConfig = new ServerConfigRecord(container, serverAddress, AsymmetricEncryptions.ECIES);
        var server = new SandnodeServer(node, serverConfig);
        session = server.createSession(pair.socket1, Connection.HUB2AGENT);
        session.start();

        Address clientAddress = new Address("127.0.0.1", 5252);
        client = new SandnodeClient(clientAddress, new TestAgent(), NodeType.HUB, () -> SymmetricEncryptions.AES);
        var clientChainManager = new BaseClientChainManager(client) {};
        client.start(clientChainManager, pair.socket2);
    }
}
