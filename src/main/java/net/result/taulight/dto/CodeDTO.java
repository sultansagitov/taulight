package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteCodeDTO.class, name = "invite")
})
public abstract class CodeDTO {
    @JsonProperty
    public String code;

    public CodeDTO() {}

    protected CodeDTO(String code) {
        this.code = code;
    }
}
