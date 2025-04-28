package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

/**
 * Abstract Data Transfer Object representing a generic code (such as an invite code).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteCodeDTO.class, name = "invite")
})
public abstract class CodeDTO {
    /** The actual code string. */
    @JsonProperty
    public String code;
    /** Creation date of the invite. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("creation-date")
    public ZonedDateTime creationDate;
    /** Date when the invite was activated, if applicable. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("activation-date")
    public @Nullable ZonedDateTime activationDate;
    /** Expiration date of the invite. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("expires-date")
    public ZonedDateTime expiresDate;

    /** Default constructor. */
    public CodeDTO() {}

    /**
     * Constructs a CodeDTO with a specified code.
     *
     * @param code the code string
     */
    protected CodeDTO(String code) {
        this.code = code;
    }
}
