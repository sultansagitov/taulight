package net.result.sandnode.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import net.result.sandnode.db.EncryptionConverter;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;

@SuppressWarnings("unused")
@Entity
public class KeyStorageEntity extends BaseEntity {
    @Convert(converter = EncryptionConverter.class)
    public Encryption encryption;
    public String encodedKey;

    @SuppressWarnings("unused")
    public KeyStorageEntity() {}

    public KeyStorageEntity(Encryption encryption, String encodedKey) {
        this.encryption = encryption;
        this.encodedKey = encodedKey;
    }

    public Encryption encryption() {
        return encryption;
    }

    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public String encodedKey() {
        return encodedKey;
    }

    public void setEncodedKey(String encodedKey) {
        this.encodedKey = encodedKey;
    }

    public PublicKeyResponse toDTO(String sender) {
        Encryption encryption = encryption();
        AsymmetricKeyStorage keyStorage = encryption.asymmetric()
                .publicKeyConvertor()
                .toKeyStorage(encodedKey());

        return new PublicKeyResponse(new Headers().setValue("sender", sender), keyStorage);
    }
}
