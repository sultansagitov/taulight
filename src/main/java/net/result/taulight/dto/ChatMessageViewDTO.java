package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.db.MessageEntity;
import net.result.taulight.db.MessageFileRepository;
import net.result.taulight.db.ReactionTypeEntity;
import net.result.taulight.db.TauMemberEntity;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Data Transfer Object (DTO) representing a chat message along with its metadata and reactions.
 */
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

    /** Default constructor. */
    public ChatMessageViewDTO() {
    }

    /**
     * Constructs a DTO based on a given {@link MessageEntity}.
     *
     * @param messageFileRepo the repository to fetch file attachments
     * @param message         the message entity
     * @throws DatabaseException if file loading fails
     */
    public ChatMessageViewDTO(MessageFileRepository messageFileRepo, MessageEntity message) throws DatabaseException {
        setID(message.id());
        setCreationDate(message.creationDate());

        Map<String, List<String>> result = new HashMap<>();
        message.reactionEntries().forEach((entry) -> {
            ReactionTypeEntity type = entry.reactionType();
            TauMemberEntity member = entry.member();

            String reaction = "%s:%s".formatted(type.reactionPackage().name(), type.name());

            result.computeIfAbsent(reaction, k -> new ArrayList<>()).add(member.member().nickname());
        });
        setReactions(result);

        setMessage(new ChatMessageInputDTO(messageFileRepo, message));
    }

    /**
     * Sets the unique identifier for this message.
     *
     * @param id the UUID to assign
     */
    public void setID(UUID id) {
        this.id = id;
    }

    /**
     * Sets the timestamp for when this message was created.
     *
     * @param creationDate the creation date to set
     */
    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Sets the message content DTO.
     *
     * @param message the message content
     */
    public void setMessage(ChatMessageInputDTO message) {
        this.message = message;
    }

    /**
     * Sets the reactions map manually.
     *
     * @param reactions a map of reaction type to member nicknames
     */
    public void setReactions(Map<String, List<String>> reactions) {
        this.reactions = reactions;
    }

    @Override
    public String toString() {
        return "<ChatMessageViewDTO id=%s created=%s message=%s reactions=%s>"
                .formatted(id, creationDate, message, reactions.keySet());
    }
}
