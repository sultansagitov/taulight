package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.MessageFileEntity;

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

    public NamedFileDTO(MessageFileEntity entity) {
        id = entity.id();
        filename = entity.originalName();
        contentType = entity.file().contentType();
    }
}
