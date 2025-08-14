package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class NamedFileDTO {
    @JsonProperty
    public UUID id;
    @JsonProperty
    public String filename;
    @JsonProperty("content-type")
    public String contentType;

    @SuppressWarnings("unused")
    public NamedFileDTO() {}

    public NamedFileDTO(UUID id, String filename, String contentType) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
    }
}
