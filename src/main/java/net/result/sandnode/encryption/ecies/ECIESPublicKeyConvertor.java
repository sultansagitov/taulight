package net.result.sandnode.encryption.ecies;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.CreatingKeyException;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECIESPublicKeyConvertor implements AsymmetricConvertor {

    private static final ECIESPublicKeyConvertor INSTANCE = new ECIESPublicKeyConvertor();

    public static ECIESPublicKeyConvertor instance() {
        return INSTANCE;
    }

    private ECIESPublicKeyConvertor() {
    }

    @Override
    public @NotNull ECIESKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(encodedString);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            return new ECIESKeyStorage(publicKey);
        } catch (Exception e) {
            throw new CreatingKeyException(AsymmetricEncryptions.ECIES, e);
        }
    }

    @Override
    public @NotNull String toEncodedString(@NotNull KeyStorage keyStorage) throws CannotUseEncryption {
        Base64.Encoder encoder = Base64.getEncoder();
        ECIESKeyStorage eciesKeyStorage = (ECIESKeyStorage) keyStorage.expect(AsymmetricEncryptions.ECIES);
        byte[] encoded = eciesKeyStorage.publicKey().getEncoded();
        return encoder.encodeToString(encoded);
    }
}
