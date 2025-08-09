package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeResponseDTO {
    public static class Check {
        @JsonProperty
        public CodeDTO code;

        public Check() {}

        public Check(CodeDTO code) {
            this.code = code;
        }
    }

    @JsonProperty
    public Check check;

    public CodeResponseDTO() {}

    public CodeResponseDTO(Check check) {
        this.check = check;
    }
}
