package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.EncryptedKeyEntity;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.EncryptionException;

import java.util.Base64;
import java.util.UUID;

public class DEKDTO {
    @JsonProperty
    public UUID id;

    @JsonProperty("encrypted-key")
    public String encryptedKey;

    @SuppressWarnings("unused")
    public DEKDTO() {}

    public DEKDTO(EncryptedKeyEntity entity) {
        encryptedKey = entity.encryptedKey();
        id = entity.id();
    }

    public DEKDTO(KeyDTO encryptor, KeyStorage keyStorage) throws CryptoException, EncryptionException {
        this.encryptedKey = getEncrypted(encryptor.keyStorage(), keyStorage);
    }

    public static String getEncrypted(KeyStorage encryptor, KeyStorage keyStorage) throws EncryptionException, CryptoException {
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

    public KeyStorage decrypt(KeyStorage personalKey) throws WrongKeyException, CannotUseEncryption,
            PrivateKeyNotFoundException, DecryptionException, NoSuchEncryptionException, EncryptionTypeException,
            CreatingKeyException {
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
