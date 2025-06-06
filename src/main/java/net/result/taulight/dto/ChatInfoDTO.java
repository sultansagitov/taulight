package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.PrivateKeyNotFoundException;
import net.result.sandnode.exception.crypto.WrongKeyException;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.db.GroupEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.db.TauMemberEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

/**
 * Data Transfer Object representing information about a chat (group or dialog).
 */
public class ChatInfoDTO implements Comparable<ChatInfoDTO> {
    /** Type of the chat (Group, Dialog, or Not Found). */
    @JsonProperty("type")
    public ChatType chatType;
    /** Unique identifier of the chat. */
    @JsonProperty
    public UUID id;
    /** Title of the group (only applicable for group chats). */
    @JsonProperty("group-title")
    public String title;
    /** Nickname of the group owner (only applicable for group chats). */
    @JsonProperty("group-owner")
    public String ownerID;
    /** Indicates whether the group is owned by the current member. */
    @JsonProperty("group-is-my")
    public boolean groupIsMy;
    /** Nickname of the other participant (only applicable for dialog chats). */
    @JsonProperty("dialog-other")
    public String otherNickname;
    /** Date and time when the chat was created (in UTC). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("creation-at")
    public ZonedDateTime creationDate;
    /** Avatar ID of the chat. May be null. */
    @JsonProperty("avatar")
    public @Nullable UUID avatar;
    /** Information about the last message sent in the chat. May be null. */
    @JsonProperty("last-message")
    public @Nullable ChatMessageViewDTO lastMessage;
    /** Information about the last message sent in the chat. May be null. */
    @JsonIgnore
    public @Nullable String decryptedMessage;

    /**
     * Compares this ChatInfoDTO with another based on creation date.
     *
     * @param o the other ChatInfoDTO to compare against
     * @return comparison result based on creation date
     */
    @Override
    public int compareTo(@NotNull ChatInfoDTO o) {
        if (creationDate == null && o.creationDate == null) return 0;
        if (creationDate == null) return -1;
        if (o.creationDate == null) return 1;
        return creationDate.compareTo(o.creationDate);
    }

    public void decrypt(SandnodeClient client) throws KeyStorageNotFoundException, WrongKeyException,
            CannotUseEncryption, PrivateKeyNotFoundException, DecryptionException {
        if (lastMessage != null) {
            UUID keyID = lastMessage.message.keyID;
            if (keyID != null) {
                KeyStorage DEK = ((Agent) client.node).config.loadDEK(keyID);
                decryptedMessage = DEK.encryption().decrypt(Base64.getDecoder().decode(lastMessage.message.content), DEK);
            } else {
                decryptedMessage = lastMessage.message.content;
            }
        }
    }

    /**
     * Enum representing types of chats.
     */
    public enum ChatType {
        /** A group chat. */
        @JsonProperty("gr") GROUP,
        /** A direct dialog between two users. */
        @JsonProperty("dl") DIALOG,
        /** A placeholder indicating the chat was not found. */
        @JsonProperty("no") NOT_FOUND
    }

    /** Private constructor to enforce usage of static factory methods. */
    private ChatInfoDTO() {}

    /**
     * Creates a ChatInfoDTO for a group.
     *
     * @param group     the group entity from the database
     * @param member      the member requesting the data
     * @param infoProps   a collection of properties to include in the response
     * @param lastMessage the last message in the group (nullable)
     * @return a populated ChatInfoDTO for a group
     */
    public static ChatInfoDTO group(
            GroupEntity group,
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps,
            ChatMessageViewDTO lastMessage
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.GROUP;
        if (infoProps.contains(ChatInfoPropDTO.groupID)) info.id = group.id();
        if (infoProps.contains(ChatInfoPropDTO.createdAt)) info.creationDate = group.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.groupTitle)) info.title = group.title();
        if (infoProps.contains(ChatInfoPropDTO.groupOwner)) info.ownerID = group.owner().member().nickname();
        if (infoProps.contains(ChatInfoPropDTO.groupIsMy)) info.groupIsMy = group.owner() == member;
        if (infoProps.contains(ChatInfoPropDTO.lastMessage)) info.lastMessage = lastMessage;
        if (infoProps.contains(ChatInfoPropDTO.hasAvatar))
            info.avatar = group.avatar() != null ? group.avatar().id() : null;
        return info;
    }

    /**
     * Constructs a ChatInfoDTO for a dialog.
     *
     * @param dialog      the dialog entity from the database
     * @param member      the member requesting the data
     * @param infoProps   a collection of properties to include in the response
     * @param lastMessage the last message in the dialog (nullable)
     *
     * @return a populated ChatInfoDTO for a dialog
     */
    public static ChatInfoDTO dialog(
            DialogEntity dialog,
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps,
            ChatMessageViewDTO lastMessage
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.DIALOG;
        if (infoProps.contains(ChatInfoPropDTO.dialogID)) info.id = dialog.id();
        if (infoProps.contains(ChatInfoPropDTO.createdAt)) info.creationDate = dialog.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.dialogOther))
            info.otherNickname = dialog.otherMember(member).member().nickname();
        if (infoProps.contains(ChatInfoPropDTO.lastMessage)) info.lastMessage = lastMessage;
        if (infoProps.contains(ChatInfoPropDTO.hasAvatar)) {
            FileEntity avatar = dialog.otherMember(member).member().avatar();
            info.avatar = avatar != null ? avatar.id() : null;
        }

        return info;
    }

    /**
     * Constructs a ChatInfoDTO indicating that the chat could not be found.
     *
     * @param chatID the UUID of the non-existent chat
     * @return a ChatInfoDTO marked as NOT_FOUND
     */
    public static ChatInfoDTO chatNotFound(UUID chatID) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.NOT_FOUND;
        info.id = chatID;
        return info;
    }
}
