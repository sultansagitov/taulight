package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.KeyConvertorUtil;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAPublicKeyConvertor implements IRSAConvertor {
    private static final Logger LOGGER = LogManager.getLogger(RSAPublicKeyConvertor.class);
    private static final RSAPublicKeyConvertor instance = new RSAPublicKeyConvertor();

    private RSAPublicKeyConvertor() {
    }

    public static RSAPublicKeyConvertor getInstance() {
        return instance;
    }

    @Override
    public @NotNull RSAKeyStorage toKeyStorage(@NotNull String PEMString) throws CreatingKeyException {
        String cleanKey = KeyConvertorUtil.removePEM(PEMString);
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(cleanKey);

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

    public @NotNull String toPEM(@NotNull RSAKeyStorage rsaKeyStorage) {
        PublicKey publicKey = rsaKeyStorage.getPublicKey();
        String base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return KeyConvertorUtil.makePEM(base64PublicKey, "PUBLIC KEY");
    }

    @Override
    public @NotNull String toPEM(@NotNull IKeyStorage keyStorage) throws ReadingKeyException {
        if (keyStorage instanceof RSAKeyStorage rsaKeyStorage) {
            return toPEM(rsaKeyStorage);
        } else {
            throw new ReadingKeyException("Key storage is not instance of RSAKeyStorage");
        }
    }

}
