package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogPasswdRequestDTO {
    @JsonProperty
    public String nickname;
    @JsonProperty
    public String password;
    @JsonProperty
    public String device;

    @SuppressWarnings("unused")
    public LogPasswdRequestDTO() {}

    public LogPasswdRequestDTO(String nickname, String password, String device) {
        this.nickname = nickname;
        this.password = password;
        this.device = device;
    }
}
