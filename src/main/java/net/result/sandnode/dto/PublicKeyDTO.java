package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import org.jetbrains.annotations.NotNull;

public class PublicKeyDTO {
    @JsonProperty
    public String encryption;

    @JsonProperty
    public String encoded;

    @SuppressWarnings("unused")
    public PublicKeyDTO() {}

    public PublicKeyDTO(@NotNull AsymmetricKeyStorage keyStorage) throws CannotUseEncryption {
        this.encryption = keyStorage.encryption().name();
        this.encoded = keyStorage.encodedPublicKey();
    }
}
