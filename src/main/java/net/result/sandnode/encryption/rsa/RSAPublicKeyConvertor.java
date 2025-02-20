package net.result.sandnode.encryption.rsa;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.exception.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAPublicKeyConvertor implements AsymmetricConvertor {
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

    @NotNull
    private RSAKeyStorage toKeyStorage(byte @NotNull [] bytes) throws CreatingKeyException {
        KeyFactory keyFactory;
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);

        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }

        PublicKey publicKey;
        try {
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new CreatingKeyException(AsymmetricEncryptions.RSA, e);
        }
        return new RSAKeyStorage(publicKey);
    }

    @Override
    public @NotNull String toEncodedString(@NotNull KeyStorage keyStorage) {
        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
        PublicKey publicKey = rsaKeyStorage.publicKey();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

}
