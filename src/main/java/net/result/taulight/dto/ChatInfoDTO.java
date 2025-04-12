package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.MemberEntity;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.DialogEntity;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public class ChatInfoDTO {
    @JsonProperty("type")
    public ChatType chatType;
    @JsonProperty
    public UUID id;
    @JsonProperty("channel-title")
    public String title;
    @JsonProperty("channel-owner")
    public String ownerID;
    @JsonProperty("channel-is-my")
    public boolean channelIsMy;
    @JsonProperty("dialog-other")
    public String otherNickname;
    @JsonProperty("creation-at")
    public ZonedDateTime creationDate;

    public enum ChatType {
        @JsonProperty("cn") CHANNEL,
        @JsonProperty("dl") DIALOG,
        @JsonProperty("no") NOT_FOUND
    }

    private ChatInfoDTO() {
    }

    public static ChatInfoDTO channel(
            ChannelEntity channel,
            MemberEntity member,
            Collection<ChatInfoPropDTO> infoProps
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.CHANNEL;
        if (infoProps.contains(ChatInfoPropDTO.channelID)) info.id = channel.id();
        if (infoProps.contains(ChatInfoPropDTO.channelCreatedAt)) info.creationDate = channel.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.channelTitle)) info.title = channel.title();
        if (infoProps.contains(ChatInfoPropDTO.channelOwner)) info.ownerID = channel.owner().nickname();
        if (infoProps.contains(ChatInfoPropDTO.channelIsMy))
            info.channelIsMy = channel.owner().id().equals(member.id());
        return info;
    }

    public static ChatInfoDTO dialog(
            DialogEntity dialog,
            MemberEntity member,
            Collection<ChatInfoPropDTO> infoProps
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.DIALOG;
        if (infoProps.contains(ChatInfoPropDTO.dialogID)) info.id = dialog.id();
        if (infoProps.contains(ChatInfoPropDTO.dialogCreatedAt)) info.creationDate = dialog.creationDate();
        if (infoProps.contains(ChatInfoPropDTO.dialogOther))
            info.otherNickname = dialog.otherMember(member).nickname();
        return info;
    }

    public static ChatInfoDTO chatNotFound(UUID chatID) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatType.NOT_FOUND;
        info.id = chatID;
        return info;
    }
}
