package net.result.sandnode.encryption.rsa;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;

public class RSAPrivateKeyConvertor implements IAsymmetricConvertor {
    private static final Logger LOGGER = LogManager.getLogger(RSAPrivateKeyConvertor.class);
    private static final RSAPrivateKeyConvertor INSTANCE = new RSAPrivateKeyConvertor();

    public static RSAPrivateKeyConvertor instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull RSAKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(encodedString);
        return toKeyStorage(bytes);
    }

    @Override
    public @NotNull RSAKeyStorage toKeyStorage(byte @NotNull [] bytes) throws CreatingKeyException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        PrivateKey privateKey;
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new CreatingKeyException(RSA, e);
        }
        return new RSAKeyStorage(privateKey);
    }

    @Override
    public @NotNull String toEncodedString(@NotNull IKeyStorage keyStorage) {
        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
        PrivateKey privateKey = rsaKeyStorage.getPrivateKey();
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

}
