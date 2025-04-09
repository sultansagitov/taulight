package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.MemberEntity;
import net.result.taulight.db.ReactionTypeEntity;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public Map<String, Collection<String>> reactions() {
        return reactions;
    }

    public void setMessages(ChatMessageInputDTO message) {
        this.message = message;
    }

    public void setReactions(Map<String, Collection<String>> reactions) {
        this.reactions = reactions;
    }

    public void mapReactionsFromEntities(@NotNull Map<ReactionTypeEntity, Collection<MemberEntity>> reactionMap) {
        Map<String, Collection<String>> result = new HashMap<>();
        reactionMap.forEach((type, member) -> {
            String reaction = "%s:%s".formatted(type.packageName(), type.name());
            Set<String> memberList = member.stream().map(MemberEntity::nickname).collect(Collectors.toSet());
            result.put(reaction, memberList);
        });
        setReactions(result);
    }

    @Override
    public String toString() {
        return "<ChatMessageViewDTO id=%s created=%s message=%s reactions=%s>"
            .formatted(id(), getCreationDate(), message, reactions.keySet());
    }
}