package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class ChatMessage {
    @JsonProperty
    private UUID id;
    @JsonProperty
    private UUID chatID;
    @JsonProperty
    private String content;
    @JsonProperty
    private ZonedDateTime ztd;
    @JsonProperty
    private String memberID;

    public ChatMessage() {}

    public UUID id() {
        return id;
    }

    public UUID chatID() {
        return chatID;
    }

    public String content() {
        return content;
    }

    public ZonedDateTime ztd() {
        return ztd;
    }

    public String memberID() {
        return memberID;
    }

    public ChatMessage setRandomID() {
        this.id = UUID.randomUUID();
        return this;
    }

    public ChatMessage setID(UUID id) {
        this.id = id;
        return this;
    }

    public ChatMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public ChatMessage setChatID(UUID chatID) {
        this.chatID = chatID;
        return this;
    }

    public ChatMessage setZtd(ZonedDateTime ztd) {
        this.ztd = ztd;
        return this;
    }

    public ChatMessage setMemberID(String memberID) {
        this.memberID = memberID;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChatMessage) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.chatID, that.chatID) &&
                Objects.equals(this.content, that.content) &&
                Objects.equals(this.ztd, that.ztd) &&
                Objects.equals(this.memberID, that.memberID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatID, content, ztd, memberID);
    }

    @Override
    public String toString() {
        return "<ChatMessage id=%s, chatID=%s, content=%s, ztd=%s, memberID=%s>"
                .formatted(id, chatID, content, ztd, memberID);
    }

}
