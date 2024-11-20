package net.result.main;

import net.result.openhelo.HeloHub;
import net.result.sandnode.config.HubPropertiesConfig;
import net.result.sandnode.config.ServerPropertiesConfig;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeyReader;

public class RunHubWork implements IWork {

    @Override
    public void run() throws Exception {
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        HubPropertiesConfig hubConfig = new HubPropertiesConfig();
        IAsymmetricEncryption mainEncryption = hubConfig.getMainEncryption();
        IAsymmetricKeyReader keyReader = mainEncryption.keyReader();
        globalKeyStorage.set(keyReader.readKeys(hubConfig));

        HeloHub hub = new HeloHub(globalKeyStorage, hubConfig);
        SandnodeServer server = new SandnodeServer(hub, new ServerPropertiesConfig());
        server.start();

        server.acceptSessions();
    }

}
