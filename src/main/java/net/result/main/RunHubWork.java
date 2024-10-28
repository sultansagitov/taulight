package net.result.main;

import net.result.openhelo.HeloHub;
import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyReader;

import java.io.IOException;

public class RunHubWork implements IWork {

    @Override
    public void run() throws IOException {
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        try {
            if (ServerConfigSingleton.useRSA()) {
                RSAKeyReader rsaKeyReader = RSAKeyReader.getInstance();
                globalKeyStorage.setRSAKeyStorage(rsaKeyReader.readKeys());
            }
        } catch (CreatingKeyException e) {
            throw new RuntimeException(e);
        }

        HeloHub hub = new HeloHub(globalKeyStorage);
        SandnodeServer server = new SandnodeServer(hub);
        server.start();
        try {
            server.acceptSessions();
        } catch (NoSuchEncryptionException | ReadingKeyException | ExpectedMessageException | DecryptionException |
                 NoSuchReqHandler | WrongNodeUsed e) {
            throw new RuntimeException(e);
        }
    }

}
