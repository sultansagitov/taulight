package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class NamedFileDTO {
    @JsonProperty
    public UUID id;
    @JsonProperty
    public String filename;
    @JsonProperty("content-type")
    public String contentType;
}
