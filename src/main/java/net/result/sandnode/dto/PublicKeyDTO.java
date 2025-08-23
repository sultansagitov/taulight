package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;


@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyDTO {
    @JsonProperty
    public String encryption;

    @JsonProperty
    public String encoded;

    public PublicKeyDTO(@NotNull AsymmetricKeyStorage keyStorage) {
        this(keyStorage.encryption().name(), keyStorage.encodedPublicKey());
    }
}
