package net.result.main;

import net.result.sandnode.exceptions.*;
import net.result.taulight.TauHub;
import net.result.sandnode.config.HubPropertiesConfig;
import net.result.sandnode.config.ServerPropertiesConfig;
import net.result.sandnode.link.Links;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IKeyStorage;

public class RunHubWork implements IWork {

    @Override
    public void run() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption,
            CreatingKeyException, KeyStorageNotFoundException, FSException, ServerStartException {
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        HubPropertiesConfig hubConfig = new HubPropertiesConfig();
        IAsymmetricEncryption mainEncryption = hubConfig.mainEncryption();
        ServerPropertiesConfig serverConfig = new ServerPropertiesConfig();
        IKeyStorage keyStorage = mainEncryption.keyReader().readKeys(serverConfig);
        globalKeyStorage.set(keyStorage);

        TauHub hub = new TauHub(globalKeyStorage, hubConfig);
        SandnodeServer server = new SandnodeServer(hub, serverConfig);
        server.start();

        String link = Links.getHubLink(server);
        System.out.println("Link for server:");
        System.out.println();
        System.out.println(link);
        System.out.println();

        try {
            server.acceptSessions();
        } catch (SocketAcceptionException ignored) {
        }
    }
}
