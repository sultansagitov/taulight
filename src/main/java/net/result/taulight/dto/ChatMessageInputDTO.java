package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.MessageEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Constructs a ChatMessageInputDTO from a {@link MessageEntity}, loading file attachments from the repository.
     *
     * @param message  the message entity to convert
     * @param fileIDs  a list of file identifiers associated with the message
     */
    public ChatMessageInputDTO(MessageEntity message, Set<UUID> fileIDs) {
        setChat(message.chat());
        setContent(message.content());
        setKeyID(message.key() != null ? message.key().id() : null);
        setSentDatetime(message.sentDatetime());
        setMember(message.member().member());
        setSys(message.sys());
        setRepliedToMessages(message.repliedToMessages().stream().map(BaseEntity::id).collect(Collectors.toSet()));
        setFileIDs(fileIDs);
    }

    public ChatMessageInputDTO setChatID(UUID chatID) {
        this.chatID = chatID;
        return this;
    }

    public ChatMessageInputDTO setKeyID(UUID keyID) {
        this.keyID = keyID;
        return this;
    }

    public ChatMessageInputDTO setChat(ChatEntity chat) {
        return setChatID(chat.id());
    }

    @SuppressWarnings("UnusedReturnValue")
    public ChatMessageInputDTO setEncryptedContent(UUID keyID, KeyStorage keyStorage, String input)
            throws EncryptionException, CryptoException {
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

    public ChatMessageInputDTO setMember(MemberEntity member) {
        return setNickname(member.nickname());
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
