package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.core.rsa.RSAKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAPublicKeyConvertor implements IAsymmetricConvertor {
    private static final Logger LOGGER = LogManager.getLogger(RSAPublicKeyConvertor.class);
    private static final RSAPublicKeyConvertor INSTANCE = new RSAPublicKeyConvertor();

    public static RSAPublicKeyConvertor instance() {
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
        KeyFactory keyFactory;
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);

        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        PublicKey publicKey;
        try {
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new CreatingKeyException(e);
        }
        return new RSAKeyStorage(publicKey);
    }

    @Override
    public @NotNull String toEncodedString(@NotNull IKeyStorage keyStorage) throws ReadingKeyException {
        if (keyStorage instanceof RSAKeyStorage) {
            RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
            PublicKey publicKey = rsaKeyStorage.getPublicKey();
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        } else {
            throw new ReadingKeyException("Key storage is not instance of RSAKeyStorage");
        }
    }

}
