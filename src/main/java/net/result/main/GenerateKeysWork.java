package net.result.main;

import net.result.sandnode.config.HubPropertiesConfig;
import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;

import java.io.IOException;

public class GenerateKeysWork implements IWork {

    @Override
    public void run() throws IOException, ReadingKeyException, NoSuchEncryptionException, ConfigurationException,
            CannotUseEncryption {
        IHubConfig hubConfig = new HubPropertiesConfig();
        FileUtil.createDir(hubConfig.getKeysDir());
        IAsymmetricEncryption mainEncryption = hubConfig.getMainEncryption();
        mainEncryption.keySaver().saveHubKeys(hubConfig, mainEncryption.generator().generate());
    }

}
