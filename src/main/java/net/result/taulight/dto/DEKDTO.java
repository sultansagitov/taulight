package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.EncryptedKeyEntity;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.Base64;
import java.util.UUID;

// TODO make test
public class DEKDTO {
    @JsonProperty
    public UUID id;

    @JsonProperty
    public String senderNickname;

    @JsonProperty
    public String receiverNickname;

    @JsonProperty
    public UUID encryptorID;

    @JsonProperty
    public String encryptedKey;

    public DEKDTO() {
    }

    public DEKDTO(String receiverNickname, UUID encryptorID, String encryptedKey) {
        this.receiverNickname = receiverNickname;
        this.encryptorID = encryptorID;
        this.encryptedKey = encryptedKey;
    }

    public DEKDTO(String receiverNickname) {
        this.receiverNickname = receiverNickname;
    }

    public DEKDTO(EncryptedKeyEntity entity) {
        this(entity.receiver().nickname(), entity.encryptor().id(), entity.encryptedKey());
        id = entity.id();
        senderNickname = entity.sender().nickname();
    }

    public DEKDTO(String receiverNickname, KeyDTO encryptor, KeyStorage keyStorage)
            throws CryptoException, EncryptionException {
        this.receiverNickname = receiverNickname;
        this.encryptorID = encryptor.keyID();

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

    public KeyStorage decrypt(SandnodeClient client) throws KeyStorageNotFoundException, WrongKeyException,
            CannotUseEncryption, PrivateKeyNotFoundException, DecryptionException, NoSuchEncryptionException,
            EncryptionTypeException, CreatingKeyException {
        KeyStorage personalKey = client.clientConfig.loadPersonalKey(encryptorID);

        String decrypted = personalKey.encryption().decrypt(Base64.getDecoder().decode(encryptedKey), personalKey);
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
