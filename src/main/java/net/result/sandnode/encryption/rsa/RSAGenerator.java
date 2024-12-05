package net.result.sandnode.encryption.rsa;

import net.result.sandnode.encryption.interfaces.IAsymmetricGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAGenerator implements IAsymmetricGenerator {
    private static final Logger LOGGER = LogManager.getLogger(RSAGenerator.class);
    private static final RSAGenerator INSTANCE = new RSAGenerator();

    public static RSAGenerator instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull RSAKeyStorage generate() {
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
