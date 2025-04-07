package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;
import net.result.taulight.db.TauChat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatMessageInputDTO {
    @JsonProperty("chat-id")
    private UUID chatID = null;
    @JsonProperty
    private String content = null;
    @JsonProperty
    private ZonedDateTime ztd = null;
    @JsonProperty
    private String nickname = null;
    @JsonProperty
    private boolean sys = false;
    @JsonProperty
    private List<UUID> replies = null;

    public ChatMessageInputDTO() {}

    public UUID chatID() {
        return chatID;
    }

    public String content() {
        return content;
    }

    public ZonedDateTime ztd() {
        return ztd;
    }

    public String nickname() {
        return nickname;
    }

    public boolean sys() {
        return sys;
    }

    public List<UUID> replies() {
        return replies;
    }

    public ChatMessageInputDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public ChatMessageInputDTO setChatID(UUID chatID) {
        this.chatID = chatID;
        return this;
    }

    public ChatMessageInputDTO setChat(TauChat chat) {
        return setChatID(chat.id());
    }

    public ChatMessageInputDTO setZtd(ZonedDateTime ztd) {
        this.ztd = ztd;
        return this;
    }

    public ChatMessageInputDTO setZtdNow() {
        return setZtd(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public ChatMessageInputDTO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public ChatMessageInputDTO setMember(Member member) {
        return setNickname(member.nickname());
    }

    public ChatMessageInputDTO setSys(boolean sys) {
        this.sys = sys;
        return this;
    }

    public ChatMessageInputDTO setReplies(List<UUID> replies) {
        this.replies = replies;
        return this;
    }

    public void addReply(UUID messageID) {
        if (replies == null) {
            replies = new ArrayList<>();
        }

        replies.add(messageID);
    }

    @Override
    public String toString() {
        return "<ChatMessageInputDTO content=%s chatID=%s ztd=%s sys=%s nickname=%s replies=%s>"
                .formatted(content, chatID, ztd, sys, nickname, replies);
    }
}
