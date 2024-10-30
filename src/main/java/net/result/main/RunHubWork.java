package net.result.main;

import net.result.openhelo.HeloHub;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyReader;

import java.io.IOException;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class RunHubWork implements IWork {

    @Override
    public void run() throws IOException {
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        HubConfig hubConfig = new HubConfig();
        try {
            if (hubConfig.useRSA()) {
                RSAKeyReader rsaKeyReader = RSAKeyReader.getInstance();
                globalKeyStorage.set(RSA, rsaKeyReader.readKeys(hubConfig));
            }
        } catch (CreatingKeyException e) {
            throw new RuntimeException(e);
        }

        HeloHub hub = new HeloHub(globalKeyStorage);
        ServerConfig serverConfig = new ServerConfig();
        SandnodeServer server = new SandnodeServer(hub, serverConfig);
        server.start();
        try {
            server.acceptSessions();
        } catch (NoSuchEncryptionException | ReadingKeyException | ExpectedMessageException | DecryptionException |
                 NoSuchReqHandler | WrongNodeUsed e) {
            throw new RuntimeException(e);
        }
    }

}
