package net.result.sandnode.util;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

public class DEKUtil {
    public static String getEncrypted(@NotNull KeyStorage encryptor, @NotNull KeyStorage keyStorage) {
        Encryption encryption = keyStorage.encryption();

        StringBuilder stringBuilder = new StringBuilder(encryption.name());

        stringBuilder.append(":");

        if (encryption.isAsymmetric()) {
            stringBuilder.append(keyStorage.asymmetric().encodedPublicKey());
        } else {
            stringBuilder.append(keyStorage.symmetric().encoded());
        }

        String orig = stringBuilder.toString();
        byte[] encrypted = encryptor.encrypt(orig);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static KeyStorage decrypt(String encryptedKey, KeyStorage personalKey) {
        String decrypted = personalKey.decrypt(Base64.getDecoder().decode(encryptedKey));
        String[] s = decrypted.split(":");
        String encryptionString = s[0];
        String encoded = s[1];

        Encryption encryption = EncryptionManager.find(encryptionString);

        KeyStorage keyStorage;
        if (encryption.isAsymmetric()) {
            keyStorage = encryption.asymmetric().publicKeyConvertor().toKeyStorage(encoded);
        } else {
            keyStorage = encryption.symmetric().toKeyStorage(Base64.getDecoder().decode(encoded));
        }

        return keyStorage;
    }
}
