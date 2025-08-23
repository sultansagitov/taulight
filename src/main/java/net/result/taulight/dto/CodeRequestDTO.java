package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class CodeRequestDTO {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Check {
        @JsonProperty
        public String code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Use {
        @JsonProperty
        public String code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupCodes {
        @JsonProperty
        public UUID chatID;
    }

    @JsonProperty
    public Check check;

    @JsonProperty
    public Use use;

    @JsonProperty
    public GroupCodes groupCodes;

    @JsonProperty
    public boolean myCodes;

    public static CodeRequestDTO check(String code) {
        CodeRequestDTO dto = new CodeRequestDTO();
        dto.check = new Check(code);
        return dto;
    }

    public static CodeRequestDTO use(String code) {
        CodeRequestDTO dto = new CodeRequestDTO();
        dto.use = new Use(code);
        return dto;
    }

    public static CodeRequestDTO groupCodes(UUID chatID) {
        CodeRequestDTO dto = new CodeRequestDTO();
        dto.groupCodes = new GroupCodes(chatID);
        return dto;
    }

    public static CodeRequestDTO myCodes() {
        CodeRequestDTO dto = new CodeRequestDTO();
        dto.myCodes = true;
        return dto;
    }
}
