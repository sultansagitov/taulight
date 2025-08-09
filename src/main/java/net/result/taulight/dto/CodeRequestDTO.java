package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeRequestDTO {
    public static class Check {
        @JsonProperty
        public String code;

        public Check() {}

        public Check(String code) {
            this.code = code;
        }
    }

    public static class Use {
        @JsonProperty
        public String code;

        public Use() {}

        public Use(String code) {
            this.code = code;
        }
    }

    @JsonProperty
    public Check check;

    @JsonProperty
    public Use use;

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
}
