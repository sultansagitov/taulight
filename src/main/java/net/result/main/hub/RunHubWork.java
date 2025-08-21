package net.result.main.hub;

import net.result.main.Work;
import net.result.main.setting.CreateReactions;
import net.result.main.setting.HubSetting;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.SocketAcceptException;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.SimpleContainer;
import net.result.taulight.hubagent.TauHub;

public class RunHubWork implements Work {
    @Override
    public void run() throws SandnodeException {
        Container container = new SimpleContainer();

        container.set(HubSetting.class);
        container.set(CreateReactions.class);

        ServerConfig serverConfig = container.get(ServerConfig.class);
        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();
        KeyStorageRegistry keyStorageRegistry = serverConfig.readKey(mainEncryption);

        HubConfig hubConfig = container.get(HubConfig.class);

        TauHub hub = new TauHub(keyStorageRegistry, hubConfig);
        SandnodeServer server = new SandnodeServer(hub, serverConfig);

        server.start();

        HubConsole console = new HubConsole(server);

        Thread thread = new Thread(() -> {
            try {
                server.acceptSessions();
            } catch (SocketAcceptException e) {
                if (!console.running) return;
                throw new RuntimeException(e);
            }
        }, "Socket-Accepting");
        thread.setDaemon(true);
        thread.start();

        console.start();
    }
}
