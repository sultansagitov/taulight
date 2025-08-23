package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class DEKResponseDTO {
    @JsonProperty
    public DEKDTO dek;
    @JsonProperty
    public String senderNickname;
}
