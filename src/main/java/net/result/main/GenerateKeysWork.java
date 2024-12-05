package net.result.main;

import net.result.sandnode.config.HubPropertiesConfig;
import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.config.ServerPropertiesConfig;
import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.exceptions.FSException;

public class GenerateKeysWork implements IWork {
    @Override
    public void run() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption, FSException {
        IHubConfig hubConfig = new HubPropertiesConfig();
        IServerConfig serverConfig = new ServerPropertiesConfig();
        IAsymmetricEncryption mainEncryption = hubConfig.mainEncryption();
        mainEncryption.keySaver().saveServerKeys(serverConfig, mainEncryption.generator().generate());
    }
}
