package net.result.sandnode.util;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.ImpossibleRuntimeException;

public class EncryptionUtil {
    public static boolean isPublicKeysEquals(AsymmetricKeyStorage key1, AsymmetricKeyStorage key2) {
        IAsymmetricEncryption encryption = key1.encryption();
        if (encryption == key2.encryption()) {
            try {
                String stringFromFile = key1.encodedPublicKey();
                String stringFromLink = key2.encodedPublicKey();
                return stringFromFile.equals(stringFromLink);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }
        }

        return false;
    }
}
