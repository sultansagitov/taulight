package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.EncryptionException;

import java.util.Base64;
import java.util.UUID;

public class PersonalKeyDTO {
    @JsonProperty
    public String nickname;

    @JsonProperty
    public UUID encryptorID;

    @JsonProperty
    public String encryptedKey;

    public PersonalKeyDTO() {
    }

    public PersonalKeyDTO(String nickname, UUID encryptorID, String encryptedKey) {
        this.nickname = nickname;
        this.encryptorID = encryptorID;
        this.encryptedKey = encryptedKey;
    }

    public PersonalKeyDTO(String nickname, KeyDTO encryptor, KeyDTO keyDTO)
            throws CryptoException, EncryptionException {
        this.nickname = nickname;
        this.encryptorID = encryptor.keyID();

        KeyStorage keyStorage = keyDTO.keyStorage();
        Encryption encryption = keyStorage.encryption();

        StringBuilder stringBuilder = new StringBuilder(encryption.name());

        stringBuilder.append(":");

        if (encryption.isAsymmetric()) {
            stringBuilder.append(encryption.asymmetric().publicKeyConvertor().toEncodedString(keyStorage));
        } else {
            stringBuilder.append(keyStorage.symmetric().encoded());
        }

        String orig = stringBuilder.toString();
        byte[] encrypted = encryptor.keyStorage().encryption().encrypt(orig, encryptor.keyStorage());
        this.encryptedKey = Base64.getEncoder().encodeToString(encrypted);
    }
}
