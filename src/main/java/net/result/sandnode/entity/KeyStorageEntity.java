package net.result.sandnode.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.db.EncryptionConverter;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class KeyStorageEntity extends BaseEntity {
    @Convert(converter = EncryptionConverter.class)
    public Encryption encryption;
    public String encodedKey;

    public PublicKeyResponse toDTO(String sender) {
        Encryption encryption = getEncryption();
        AsymmetricKeyStorage keyStorage = encryption.asymmetric()
                .publicKeyConvertor()
                .toKeyStorage(getEncodedKey());

        return new PublicKeyResponse(new Headers().setValue("sender", sender), keyStorage);
    }
}
