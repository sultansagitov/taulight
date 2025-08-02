package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.EncryptedKeyEntity;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.util.DEKUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DEKDTO {
    @JsonProperty
    public UUID id;

    @JsonProperty("encrypted-key")
    public String encryptedKey;

    @SuppressWarnings("unused")
    public DEKDTO() {}

    public DEKDTO(@NotNull EncryptedKeyEntity entity) {
        encryptedKey = entity.encryptedKey();
        id = entity.id();
    }

    public DEKDTO(@NotNull KeyDTO encryptor, @NotNull KeyStorage keyStorage)
            throws CryptoException, EncryptionException {
        this.encryptedKey = DEKUtil.getEncrypted(encryptor.keyStorage(), keyStorage);
    }
}
