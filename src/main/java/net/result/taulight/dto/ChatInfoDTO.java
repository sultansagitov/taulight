package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.db.TauMemberEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

/**
 * Data Transfer Object representing information about a chat (channel or dialog).
 */
public class ChatInfoDTO implements Comparable<ChatInfoDTO> {
    /** Type of the chat (Channel, Dialog, or Not Found). */
    @JsonProperty("type")
    public ChatType chatType;
    /** Unique identifier of the chat. */
    @JsonProperty
    public UUID id;
    /** Title of the channel (only applicable for channel chats). */
    @JsonProperty("channel-title")
    public String title;
    /** Nickname of the channel owner (only applicable for channel chats). */
    @JsonProperty("channel-owner")
    public String ownerID;
    /** Indicates whether the channel is owned by the current member. */
    @JsonProperty("channel-is-my")
    public boolean channelIsMy;
    /** Nickname of the other participant (only applicable for dialog chats). */
    @JsonProperty("dialog-other")
    public String otherNickname;
    /** Date and time when the chat was created (in UTC). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("creation-at")
    public ZonedDateTime creationDate;

    /** Information about the last message sent in the chat. May be null. */
    @JsonProperty("last-message")
    public @Nullable ChatMessageViewDTO lastMessage;

    /**
     * Compares this ChatInfoDTO with another based on creation date.
     *
     * @param o the other ChatInfoDTO to compare against
     * @return comparison result based on creation date
     */
    @Override
    public int compareTo(@NotNull ChatInfoDTO o) {
        return creationDate.compareTo(o.creationDate);
    }

    /**
     * Enum representing types of chats.
     */
    public enum ChatType {
        /** A channel chat. */
        @JsonProperty("cn") CHANNEL,
        /** A direct dialog between two users. */
        @JsonProperty("dl") DIALOG,
        /** A placeholder indicating the chat was not found. */
        @JsonProperty("no") NOT_FOUND
    }

    /** Private constructor to enforce usage of static factory methods. */
    private ChatInfoDTO() {}

    /**
     * Creates a ChatInfoDTO for a channel.
     *
     * @param channel     the channel entity from the database
     * @param member      the member requesting the data
     * @param infoProps   a collection of properties to include in the response
     * @param lastMessage the last message in the channel (nullable)
     * @return a populated ChatInfoDTO for a channel
     */
    public static ChatInfoDTO channel(
            ChannelEntity channel,
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps,
            ChatMessageViewDTO lastMessage
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.CHANNEL;
        if (infoProps.contains(ChatInfoPropDTO.channelID)) info.id = channel.id();
        if (infoProps.contains(ChatInfoPropDTO.channelCreatedAt)) info.creationDate = channel.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.channelTitle)) info.title = channel.title();
        if (infoProps.contains(ChatInfoPropDTO.channelOwner)) info.ownerID = channel.owner().member().nickname();
        if (infoProps.contains(ChatInfoPropDTO.channelIsMy)) info.channelIsMy = channel.owner() == member;
        if (infoProps.contains(ChatInfoPropDTO.lastMessage)) info.lastMessage = lastMessage;
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
        if (infoProps.contains(ChatInfoPropDTO.dialogCreatedAt)) info.creationDate = dialog.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.dialogOther))
            info.otherNickname = dialog.otherMember(member).member().nickname();
        if (infoProps.contains(ChatInfoPropDTO.lastMessage)) info.lastMessage = lastMessage;

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
