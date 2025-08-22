package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.encryption.interfaces.KeyStorage;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object used to send or receive chat message input data.
 */
public class ChatMessageInputDTO {
    /** Unique identifier of the chat. */
    @JsonProperty("chat-id")
    public UUID chatID = null;
    /** ID of key that was encrypted. */
    @JsonProperty("key-id")
    public UUID keyID = null;
    /** Encrypted content of the message. */
    @JsonProperty
    public String content = null;
    /** Date and time when the message was sent. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("sent-datetime")
    public ZonedDateTime sentDatetime = null;
    /** Nickname of the message sender. */
    @JsonProperty
    public String nickname = null;
    /** Indicates whether the message is a system message. */
    @JsonProperty
    public boolean sys = false;
    /** List of IDs of messages that this message replies to. */
    @JsonProperty("replied-to-messages")
    public Set<UUID> repliedToMessages = null;
    /** List of IDs of files attached to this message. */
    @JsonProperty("file-ids")
    public Set<UUID> fileIDs = null;

    /** Default constructor. */
    public ChatMessageInputDTO() {}

    public ChatMessageInputDTO(
            UUID chatID,
            UUID keyID,
            String content,
            ZonedDateTime sentDatetime,
            String nickname,
            boolean sys,
            Set<UUID> repliedToMessages,
            Set<UUID> fileIDs
    ) {
        this.chatID = chatID;
        this.keyID = keyID;
        this.content = content;
        this.sentDatetime = sentDatetime;
        this.nickname = nickname;
        this.sys = sys;
        this.repliedToMessages = repliedToMessages;
        this.fileIDs = fileIDs;
    }

    public ChatMessageInputDTO setChatID(UUID chatID) {
        this.chatID = chatID;
        return this;
    }

    public ChatMessageInputDTO setKeyID(UUID keyID) {
        this.keyID = keyID;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ChatMessageInputDTO setEncryptedContent(UUID keyID, KeyStorage keyStorage, String input) {
        setKeyID(keyID);

        String content = Base64.getEncoder().encodeToString(keyStorage.encrypt(input));
        setContent(content);

        return this;
    }

    public ChatMessageInputDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public ChatMessageInputDTO setSentDatetime(ZonedDateTime sentDatetime) {
        this.sentDatetime = sentDatetime;
        return this;
    }

    public ChatMessageInputDTO setSentDatetimeNow() {
        return setSentDatetime(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public ChatMessageInputDTO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public ChatMessageInputDTO setSys(boolean sys) {
        this.sys = sys;
        return this;
    }

    public ChatMessageInputDTO setRepliedToMessages(Set<UUID> repliedToMessages) {
        this.repliedToMessages = repliedToMessages;
        return this;
    }

    public ChatMessageInputDTO setFileIDs(Set<UUID> fileIDs) {
        this.fileIDs = fileIDs;
        return this;
    }

    @Override
    public String toString() {
        return "<ChatMessageInputDTO content=%s chatID=%s ztd=%s sys=%s nickname=%s repliedToMessages=%s fileIDs=%s>"
                .formatted(content, chatID, sentDatetime, sys, nickname, repliedToMessages, fileIDs);
    }
}
