package net.result.sandnode.encryption.ecies;

import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class ECIESKeyGenerator {
    public static @NotNull ECIESKeyStorage generate() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("ECIES", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new ImpossibleRuntimeException(e);
        }
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return new ECIESKeyStorage(keyPair.getPublic(), keyPair.getPrivate());
    }
}
