package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.MessageEntity;
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
    private ChatMessageInputDTO message;

    /** The unique identifier of the message. */
    @JsonProperty
    private UUID id = null;

    /** The timestamp of when the message was created. */
    @JsonProperty("creation-date")
    private ZonedDateTime creationDate;

    /**
     * A map of reaction types (as package:name) to the set of member nicknames who reacted.
     * Example entry: "emoji:thumbs_up" -> ["alice", "bob"]
     */
    @JsonProperty
    private Map<String, Collection<String>> reactions = new HashMap<>();

    /** Default constructor. */
    public ChatMessageViewDTO() {
    }

    /**
     * Constructs a DTO based on a given {@link MessageEntity}.
     *
     * @param message the message entity to initialize from
     */
    public ChatMessageViewDTO(MessageEntity message) {
        setID(message.id());
        setCreationDate(message.creationDate());

        Map<String, Collection<String>> result = new HashMap<>();
        message.reactionEntries().forEach((entry) -> {
            ReactionTypeEntity type = entry.reactionType();
            TauMemberEntity member = entry.member();

            String reaction = "%s:%s".formatted(type.reactionPackage().name(), type.name());

            result.computeIfAbsent(reaction, k -> new HashSet<>()).add(member.member().nickname());
        });
        setReactions(result);

        setMessage(new ChatMessageInputDTO(message));
    }

    /**
     * @return the unique identifier of the message
     */
    public UUID id() {
        return id;
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
     * @return the timestamp of message creation
     */
    public ZonedDateTime getCreationDate() {
        return creationDate;
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
     * @return the message content DTO
     */
    public ChatMessageInputDTO message() {
        return message;
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
     * @return the map of reaction types to member nicknames
     */
    public Map<String, Collection<String>> reactions() {
        return reactions;
    }

    /**
     * Sets the reactions map manually.
     *
     * @param reactions a map of reaction type to member nicknames
     */
    public void setReactions(Map<String, Collection<String>> reactions) {
        this.reactions = reactions;
    }

    @Override
    public String toString() {
        return "<ChatMessageViewDTO id=%s created=%s message=%s reactions=%s>"
                .formatted(id(), getCreationDate(), message(), reactions().keySet());
    }
}
