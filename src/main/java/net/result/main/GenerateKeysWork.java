package net.result.main;

import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.exception.crypto.SavingKeyException;
import net.result.sandnode.util.SimpleContainer;

public class GenerateKeysWork implements Work {
    @Override
    public void run() throws NoSuchEncryptionException, ConfigurationException, FSException, EncryptionTypeException,
            SavingKeyException {
        ServerConfig serverConfig = new ServerPropertiesConfig(new SimpleContainer());
        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();
        AsymmetricKeyStorage keyStorage = mainEncryption.generate();

        serverConfig.saveKey(keyStorage);
    }
}
