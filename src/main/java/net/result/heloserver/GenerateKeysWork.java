package net.result.heloserver;

import net.result.sandnode.config.PathUtil;
import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeySaver;
import net.result.sandnode.util.encryption.rsa.RSAGenerator;

import java.io.IOException;

public class GenerateKeysWork implements IWork {

    @Override
    public void run() throws IOException, ReadingKeyException {
        PathUtil.createDir(ServerConfigSingleton.getKeysDir());
        if (ServerConfigSingleton.useRSA()) RSAKeySaver.getInstance().saveKeys(RSAGenerator.getInstance().generateKeyStorage());
    }

}
