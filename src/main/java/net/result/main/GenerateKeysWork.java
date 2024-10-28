package net.result.main;

import net.result.sandnode.config.PathUtil;
import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeySaver;

import java.io.IOException;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class GenerateKeysWork implements IWork {

    @Override
    public void run() throws IOException, ReadingKeyException {
        PathUtil.createDir(ServerConfigSingleton.getKeysDir());
        if (ServerConfigSingleton.useRSA())
            RSAKeySaver.getInstance().saveKeys(RSA.generator().generateKeyStorage());
    }

}
