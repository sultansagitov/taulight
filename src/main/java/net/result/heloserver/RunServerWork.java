package net.result.heloserver;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyReader;

import java.io.IOException;

public class RunServerWork implements IWork {

    @Override
    public void run() throws IOException {
        final GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();
        try {
            if (ServerConfigSingleton.useRSA()) {
                RSAKeyReader rsaKeyReader = RSAKeyReader.getInstance();
                globalKeyStorage.setRSAKeyStorage(rsaKeyReader.readKeys());
            }
        } catch (CreatingKeyException e) {
            throw new RuntimeException(e);
        }

        final SandnodeServer server = new SandnodeServer(globalKeyStorage);
        server.start();
    }

}
