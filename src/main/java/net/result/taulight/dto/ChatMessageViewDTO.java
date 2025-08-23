package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Data Transfer Object (DTO) representing a chat message along with its metadata and reactions.
 */
@Setter
public class ChatMessageViewDTO {

    /** The actual chat message content and metadata (e.g. text, sender). */
    @JsonProperty
    public ChatMessageInputDTO message;

    /** The unique identifier of the message. */
    @JsonProperty
    public UUID id = null;

    /** The timestamp of when the message was created. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("creation-date")
    public ZonedDateTime creationDate;

    /**
     * A map of reaction types (as package:name) to the set of member nicknames who reacted.
     * Example entry: "emoji:thumbs_up" -> ["alice", "bob"]
     */
    @JsonProperty
    public Map<String, List<String>> reactions = new HashMap<>();

    /**
     * A list of files associated with the message, each represented as a {@link NamedFileDTO}.
     */
    @JsonProperty
    public List<NamedFileDTO> files;

    /** Default constructor. */
    public ChatMessageViewDTO() {}

    public ChatMessageViewDTO(
            ChatMessageInputDTO message,
            UUID id,
            ZonedDateTime creationDate,
            Map<String, List<String>> reactions,
            List<NamedFileDTO> files
    ) {
        this.message = message;
        this.id = id;
        this.creationDate = creationDate;
        this.reactions = reactions;
        this.files = files;
    }

    @Override
    public String toString() {
        return "<ChatMessageViewDTO id=%s created=%s message=%s reactions=%s>"
                .formatted(id, creationDate, message, reactions.keySet());
    }
}
