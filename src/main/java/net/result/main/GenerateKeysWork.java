package net.result.main;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;

import java.io.IOException;

public class GenerateKeysWork implements IWork {

    @Override
    public void run() throws IOException, ReadingKeyException {
        HubConfig hubConfig = new HubConfig();
        FileUtil.createDir(hubConfig.getKeysDir());
        Encryption mainEncryption = hubConfig.getMainEncryption();
        try {
            Asymmetric.getKeySaver(mainEncryption).saveHubKeys(hubConfig, mainEncryption.generator().generateKeyStorage());
        } catch (CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
    }

}
