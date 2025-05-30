package net.result.sandnode.db;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import net.result.sandnode.encryption.interfaces.Encryption;

@SuppressWarnings("unused")
@Entity
public class KeyStorageEntity extends BaseEntity {
    @Convert(converter = EncryptionConverter.class)
    Encryption encryption;
    String encodedKey;

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
}
