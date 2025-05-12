package net.result.main;

import net.result.sandnode.config.ServerConfig;
import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.exception.crypto.SavingKeyException;
import net.result.sandnode.util.Container;

public class GenerateKeysWork implements IWork {
    @Override
    public void run() throws NoSuchEncryptionException, ConfigurationException, FSException, EncryptionTypeException,
            SavingKeyException {
        ServerConfig serverConfig = new ServerPropertiesConfig(new Container());
        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();
        AsymmetricKeyStorage keyStorage = mainEncryption.generate();

        serverConfig.saveKey(keyStorage);
    }
}
