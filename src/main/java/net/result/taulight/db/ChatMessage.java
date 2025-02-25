package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class ChatMessage {
    @JsonProperty("chat-id")
    private UUID chatID = null;
    @JsonProperty
    private String content = null;
    @JsonProperty
    private ZonedDateTime ztd = null;
    @JsonProperty("member-id")
    private String memberID = null;

    public ChatMessage() {}

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

    public ChatMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public ChatMessage setChatID(UUID chatID) {
        this.chatID = chatID;
        return this;
    }

    public ChatMessage setChat(TauChat chat) {
        return setChatID(chat.id());
    }

    public ChatMessage setZtd(ZonedDateTime ztd) {
        this.ztd = ztd;
        return this;
    }

    public ChatMessage setZtdNow() {
        return setZtd(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public ChatMessage setMemberID(String memberID) {
        this.memberID = memberID;
        return this;
    }

    public ChatMessage setMember(Member member) {
        return setMemberID(member.id());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChatMessage) obj;
        return Objects.equals(this.chatID, that.chatID) &&
                Objects.equals(this.content, that.content) &&
                Objects.equals(this.ztd, that.ztd) &&
                Objects.equals(this.memberID, that.memberID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatID, content, ztd, memberID);
    }

    @Override
    public String toString() {
        return "<ChatMessage chatID=%s, content=%s, ztd=%s, memberID=%s>"
                .formatted(chatID, content, ztd, memberID);
    }

}
