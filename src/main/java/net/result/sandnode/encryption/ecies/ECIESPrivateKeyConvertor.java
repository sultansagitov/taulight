package net.result.sandnode.encryption.ecies;

import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.CreatingKeyException;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static net.result.sandnode.encryption.AsymmetricEncryptions.ECIES;

public class ECIESPrivateKeyConvertor implements AsymmetricConvertor {
    private static final ECIESPrivateKeyConvertor INSTANCE = new ECIESPrivateKeyConvertor();

    public static ECIESPrivateKeyConvertor instance() {
        return INSTANCE;
    }

    private ECIESPrivateKeyConvertor() {
    }

    @Override
    public @NotNull ECIESKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(encodedString);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            return new ECIESKeyStorage(privateKey);
        } catch (Exception e) {
            throw new CreatingKeyException(ECIES, e);
        }
    }

    @Override
    public @NotNull String toEncodedString(@NotNull KeyStorage keyStorage) throws CannotUseEncryption {
        Base64.Encoder encoder = Base64.getEncoder();
        ECIESKeyStorage eciesKeyStorage = (ECIESKeyStorage) keyStorage.expect(ECIES);
        byte[] encoded = eciesKeyStorage.privateKey().getEncoded();
        return encoder.encodeToString(encoded);
    }
}
