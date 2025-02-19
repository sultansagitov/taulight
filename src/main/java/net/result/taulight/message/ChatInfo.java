package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDirect;

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
    @JsonProperty("direct-other")
    public String otherMemberID;

    public enum ChatType {
        @JsonProperty("cn") CHANNEL,
        @JsonProperty("dm") DIRECT,
        @JsonProperty("no") NOT_FOUND
    }

    private ChatInfo() {
    }

    public static ChatInfo byChat(TauChat chat, Member member, Collection<ChatInfoProp> infoProps) {
        if (chat instanceof TauChannel channel) return (ChatInfo.channel(channel, member, infoProps));
        if (chat instanceof TauDirect direct) return (ChatInfo.direct(direct, member, infoProps));
        return (chatNotFound(chat.id()));
    }

    private static ChatInfo channel(TauChannel channel, Member member, Collection<ChatInfoProp> infoProps) {
        ChatInfo info = new ChatInfo();
        info.chatType = ChatType.CHANNEL;
        if (infoProps.contains(ChatInfoProp.channelID)) info.id = channel.id();
        if (infoProps.contains(ChatInfoProp.channelTitle)) info.title = channel.title();
        if (infoProps.contains(ChatInfoProp.channelOwner)) info.ownerID = channel.owner().id();
        if (infoProps.contains(ChatInfoProp.channelIsMy)) info.channelIsMy = channel.owner().equals(member);
        return info;
    }

    private static ChatInfo direct(TauDirect direct, Member member, Collection<ChatInfoProp> infoProps) {
        ChatInfo info = new ChatInfo();
        info.chatType = ChatType.DIRECT;
        if (infoProps.contains(ChatInfoProp.directID)) info.id = direct.id();
        if (infoProps.contains(ChatInfoProp.directOther)) info.otherMemberID = direct.otherMember(member).id();
        return info;
    }

    public static ChatInfo chatNotFound(UUID chatID) {
        ChatInfo info = new ChatInfo();
        info.chatType = ChatType.NOT_FOUND;
        info.id = chatID;
        return info;
    }
}
