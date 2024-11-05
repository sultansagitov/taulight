package net.result.main;

import net.result.openhelo.HeloHub;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricKeyReader;

import java.io.IOException;

public class RunHubWork implements IWork {

    @Override
    public void run() throws IOException {
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        HubConfig hubConfig = new HubConfig();
        try {
            Encryption mainEncryption = hubConfig.getMainEncryption();
            IAsymmetricKeyReader keyReader = Asymmetric.getKeyReader(mainEncryption);
            globalKeyStorage.set(keyReader.readKeys(hubConfig));
        } catch (CreatingKeyException | CannotUseEncryption e) {
            throw new RuntimeException(e);
        }

        HeloHub hub = new HeloHub(globalKeyStorage, hubConfig);
        SandnodeServer server = new SandnodeServer(hub, new ServerConfig());
        server.start();

        server.acceptSessions();
    }

}
