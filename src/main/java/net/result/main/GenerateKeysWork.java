package net.result.main;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.PathUtil;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeySaver;

import java.io.IOException;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class GenerateKeysWork implements IWork {

    @Override
    public void run() throws IOException, ReadingKeyException {
        HubConfig hubConfig = new HubConfig();
        PathUtil.createDir(hubConfig.getKeysDir());
        if (hubConfig.useRSA())
            RSAKeySaver.getInstance().saveKeys(hubConfig, RSA.generator().generateKeyStorage());
    }

}
