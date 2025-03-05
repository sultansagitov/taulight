package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauDialog;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public class ChatInfo {
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
    public String otherMemberID;
    @JsonProperty("creation-at")
    public ZonedDateTime creationDate;

    public enum ChatType {
        @JsonProperty("cn") CHANNEL,
        @JsonProperty("dl") DIALOG,
        @JsonProperty("no") NOT_FOUND
    }

    private ChatInfo() {
    }

    public static ChatInfo channel(TauChannel channel, Member member, Collection<ChatInfoProp> infoProps) {
        ChatInfo info = new ChatInfo();
        info.chatType = ChatType.CHANNEL;
        if (infoProps.contains(ChatInfoProp.channelID)) info.id = channel.id();
        if (infoProps.contains(ChatInfoProp.channelCreatedAt)) info.creationDate = channel.getCreationDate();
        if (infoProps.contains(ChatInfoProp.channelTitle)) info.title = channel.title();
        if (infoProps.contains(ChatInfoProp.channelOwner)) info.ownerID = channel.owner().id();
        if (infoProps.contains(ChatInfoProp.channelIsMy)) info.channelIsMy = channel.owner().equals(member);
        return info;
    }

    public static ChatInfo dialog(TauDialog dialog, Member member, Collection<ChatInfoProp> infoProps) {
        ChatInfo info = new ChatInfo();
        info.chatType = ChatType.DIALOG;
        if (infoProps.contains(ChatInfoProp.dialogID)) info.id = dialog.id();
        if (infoProps.contains(ChatInfoProp.dialogCreatedAt)) info.creationDate = dialog.getCreationDate();
        if (infoProps.contains(ChatInfoProp.dialogOther)) info.otherMemberID = dialog.otherMember(member).id();
        return info;
    }

    public static ChatInfo chatNotFound(UUID chatID) {
        ChatInfo info = new ChatInfo();
        info.chatType = ChatType.NOT_FOUND;
        info.id = chatID;
        return info;
    }
}
