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
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RSAPrivateKeyConvertor implements IRSAConvertor {
    private static final Logger LOGGER = LogManager.getLogger(RSAPrivateKeyConvertor.class);

    @Override
    public @NotNull RSAKeyStorage toKeyStorage(@NotNull String PEMString) throws CreatingKeyException {
        final String cleanKey = KeyConvertorUtil.removePEM(PEMString);
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] bytes = decoder.decode(cleanKey);

        return new RSAPrivateKeyConvertor().toKeyStorage(bytes);
    }

    @Override
    public @NotNull RSAKeyStorage toKeyStorage(byte @NotNull [] bytes) throws CreatingKeyException {
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        final KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        final PrivateKey privateKey;
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new CreatingKeyException(e);
        }
        return new RSAKeyStorage().setPrivateKey(privateKey);
    }

    public @NotNull String toPEM(@NotNull RSAKeyStorage rsaKeyStorage) {
        final PrivateKey privateKey = rsaKeyStorage.getPrivateKey();
        final String base64PrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return KeyConvertorUtil.makePEM(base64PrivateKey, "PRIVATE KEY");
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
