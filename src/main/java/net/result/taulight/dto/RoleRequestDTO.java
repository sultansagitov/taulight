package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    public enum DataType {GET, CREATE, ADD}

    @JsonProperty("type")
    public DataType dataType = null;
    @JsonProperty("chat-id")
    public UUID chatID = null;
    @JsonProperty("role-id")
    public UUID roleID = null;
    @JsonProperty("role-name")
    public String roleName = null;

    @JsonProperty("nickname")
    public String nickname = null;
}
