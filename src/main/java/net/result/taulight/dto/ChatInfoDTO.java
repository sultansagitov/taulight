package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.db.TauMemberEntity;
import org.jetbrains.annotations.NotNull;

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
    /** Title of the channel (for channel chats). */
    @JsonProperty("channel-title")
    public String title;
    /** Nickname of the channel owner (for channel chats). */
    @JsonProperty("channel-owner")
    public String ownerID;
    /** Indicates whether the channel belongs to the current member. */
    @JsonProperty("channel-is-my")
    public boolean channelIsMy;
    /** Nickname of the other participant (for dialogs). */
    @JsonProperty("dialog-other")
    public String otherNickname;
    /** Date and time when the chat was created. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    @JsonProperty("creation-at")
    public ZonedDateTime creationDate;

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
        @JsonProperty("cn") CHANNEL,
        @JsonProperty("dl") DIALOG,
        @JsonProperty("no") NOT_FOUND
    }

    /** Private constructor to enforce usage of static factory methods. */
    private ChatInfoDTO() {}

    /**
     * Creates a ChatInfoDTO for a channel.
     *
     * @param channel the channel entity
     * @param member the current member
     * @param infoProps properties to include
     * @return a ChatInfoDTO populated for a channel
     */
    public static ChatInfoDTO channel(
            ChannelEntity channel,
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.CHANNEL;
        if (infoProps.contains(ChatInfoPropDTO.channelID)) info.id = channel.id();
        if (infoProps.contains(ChatInfoPropDTO.channelCreatedAt)) info.creationDate = channel.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.channelTitle)) info.title = channel.title();
        if (infoProps.contains(ChatInfoPropDTO.channelOwner)) info.ownerID = channel.owner().member().nickname();
        if (infoProps.contains(ChatInfoPropDTO.channelIsMy)) info.channelIsMy = channel.owner() == member;
        return info;
    }

    /**
     * Creates a ChatInfoDTO for a dialog.
     *
     * @param dialog the dialog entity
     * @param member the current member
     * @param infoProps properties to include
     * @return a ChatInfoDTO populated for a dialog
     */
    public static ChatInfoDTO dialog(
            DialogEntity dialog,
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.DIALOG;
        if (infoProps.contains(ChatInfoPropDTO.dialogID)) info.id = dialog.id();
        if (infoProps.contains(ChatInfoPropDTO.dialogCreatedAt)) info.creationDate = dialog.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.dialogOther))
            info.otherNickname = dialog.otherMember(member).member().nickname();
        return info;
    }

    /**
     * Creates a ChatInfoDTO for a non-existing chat (Not Found).
     *
     * @param chatID the ID of the missing chat
     * @return a ChatInfoDTO representing a missing chat
     */
    public static ChatInfoDTO chatNotFound(UUID chatID) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.NOT_FOUND;
        info.id = chatID;
        return info;
    }
}
