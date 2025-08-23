package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
public class CodeResponseDTO {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Check {
        @JsonProperty
        public CodeDTO code;
    }

    @JsonProperty
    public Check check;
}
