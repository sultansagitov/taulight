package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.MessageEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatMessageInputDTO {
    @JsonProperty("chat-id")
    private UUID chatID = null;
    @JsonProperty
    private String content = null;
    @JsonProperty("sent-datetime")
    private ZonedDateTime sentDatetime = null;
    @JsonProperty
    private String nickname = null;
    @JsonProperty
    private boolean sys = false;
    @JsonProperty
    private Set<UUID> repliedToMessages = null;

    public ChatMessageInputDTO() {}

    public ChatMessageInputDTO(MessageEntity message) {
        setChat(message.chat());
        setContent(message.content());
        setSentDatetime(message.sentDatetime());
        setMember(message.member().member());
        setSys(message.sys());
        setRepliedToMessages(message.repliedToMessages().stream().map(SandnodeEntity::id).collect(Collectors.toSet()));
    }

    public UUID chatID() {
        return chatID;
    }

    public ChatMessageInputDTO setChatID(UUID chatID) {
        this.chatID = chatID;
        return this;
    }

    public ChatMessageInputDTO setChat(ChatEntity chat) {
        return setChatID(chat.id());
    }

    public String content() {
        return content;
    }

    public ChatMessageInputDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public ZonedDateTime sentDatetime() {
        return sentDatetime;
    }

    public ChatMessageInputDTO setSentDatetime(ZonedDateTime sentDatetime) {
        this.sentDatetime = sentDatetime;
        return this;
    }

    public ChatMessageInputDTO setSentDatetimeNow() {
        return setSentDatetime(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public String nickname() {
        return nickname;
    }

    public ChatMessageInputDTO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public ChatMessageInputDTO setMember(MemberEntity member) {
        return setNickname(member.nickname());
    }

    public boolean sys() {
        return sys;
    }

    public ChatMessageInputDTO setSys(boolean sys) {
        this.sys = sys;
        return this;
    }

    public Set<UUID> repliedToMessages() {
        return repliedToMessages;
    }

    public ChatMessageInputDTO setRepliedToMessages(Set<UUID> repliedToMessages) {
        this.repliedToMessages = repliedToMessages;
        return this;
    }

    @Override
    public String toString() {
        return "<ChatMessageInputDTO content=%s chatID=%s ztd=%s sys=%s nickname=%s repliedToMessages=%s>"
                .formatted(content, chatID, sentDatetime, sys, nickname, repliedToMessages);
    }
}
