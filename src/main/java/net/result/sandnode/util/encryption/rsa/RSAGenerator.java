package net.result.sandnode.util.encryption.rsa;

import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricGenerator;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAGenerator implements IAsymmetricGenerator {
    private static final Logger LOGGER = LogManager.getLogger(RSAGenerator.class);
    private static final RSAGenerator instance = new RSAGenerator();

    private RSAGenerator() {}

    public static RSAGenerator getInstance() {
        return instance;
    }

    @Override
    public RSAKeyStorage generateKeyStorage() {
        KeyPairGenerator keyGen;

        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        KeyPair keyPair = keyGen.generateKeyPair();
        LOGGER.info("RSA key pair successfully generated");

        return new RSAKeyStorage(keyPair);
    }

}
