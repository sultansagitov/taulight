package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    @JsonProperty
    private boolean sys = false;

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

    public boolean sys() {
        return sys;
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

    public ChatMessage setSys(boolean sys) {
        this.sys = sys;
        return this;
    }

    @Override
    public String toString() {
        return "<ChatMessage content=%s, chatID=%s, ztd=%s, sys=%s, memberID=%s>"
                .formatted(content, chatID, ztd, sys, memberID);
    }

}
