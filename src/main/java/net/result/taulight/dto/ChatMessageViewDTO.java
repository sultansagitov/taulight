package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TaulightObject;
import net.result.taulight.dto.ChatMessageInputDTO;

import java.time.ZonedDateTime;
import java.util.*;

public class ChatMessageViewDTO {
    @JsonProperty
    private ChatMessageInputDTO message;
    @JsonProperty
    private UUID id = null;
    @JsonProperty("creation-date")
    private ZonedDateTime creationDate;
    @JsonProperty
    private Map<String, Collection<String>> reactions = new HashMap<>();

    public ChatMessageViewDTO() {
        super();
    }

    public UUID id() {
        return id;
    }

    public void setID(UUID id) {
        this.id = id;
    }

    public void setRandomID() {
        this.id = UUID.randomUUID();
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationDateNow() {
        setCreationDate(ZonedDateTime.now());
    }

    public ChatMessageInputDTO message() {
        return message;
    }

    public void setChatMessage(ChatMessageInputDTO message) {
        this.message = message;
    }

    public Map<String, Collection<String>> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, Collection<String>> reactions) {
        this.reactions = reactions;
    }

    public void addReaction(String reaction, String nickname) {
        reactions.computeIfAbsent(reaction, k -> new HashSet<>()).add(nickname);
    }

    @Override
    public String toString() {
        return "<ChatMessageViewDTO id=%s created=%s message=%s reactions=%s>"
            .formatted(id(), getCreationDate(), message, reactions.keySet());
    }
}