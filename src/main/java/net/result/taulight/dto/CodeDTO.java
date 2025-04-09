package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteTauCode.class, name = "invite")
})
public abstract class TauCode {
    @JsonProperty
    public String code;

    public TauCode() {}

    protected TauCode(String code) {
        this.code = code;
    }
}
