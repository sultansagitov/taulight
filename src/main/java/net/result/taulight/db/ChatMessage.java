package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ChatMessage {
    @JsonProperty
    private String id;
    @JsonProperty
    private String chatID;
    @JsonProperty
    private String content;
    @JsonProperty
    private ZonedDateTime ztd;
    @JsonProperty
    private String memberID;

    @SuppressWarnings("unused")
    public ChatMessage() {}

    public ChatMessage(String id, String chatID, String content, ZonedDateTime ztd, String memberID) {
        this.id = id;
        this.chatID = chatID;
        this.content = content;
        this.ztd = ztd;
        this.memberID = memberID;
    }

    public String id() {
        return id;
    }

    public String chatID() {
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
