package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauDirect;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.db.TauChat;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatResponse extends MSGPackMessage<ChatResponse.Data> {
    public enum ChatType {
        @JsonProperty("cn") CHANNEL,
        @JsonProperty("dm") DIRECT,
        @JsonProperty("no") CHAT_NOT_FOUND
    }

    public static class Info {
        @JsonProperty("type")
        public ChatType chatType;
        @JsonProperty
        public UUID id;
        @JsonProperty
        public String title;
        @JsonProperty("owner-id")
        public String ownerID;
        @JsonProperty("other-member-id")
        public String otherMemberID;

        public Info() {}

        public static Info chatNotFound(UUID chatID) {
            Info info = new Info();
            info.chatType = ChatType.CHAT_NOT_FOUND;
            info.id = chatID;
            return info;
        }

        public static Info channel(TauChannel channel) {
            Info info = new Info();
            info.chatType = ChatType.CHANNEL;
            info.id = channel.id();
            info.title = channel.title();
            info.ownerID = channel.owner().id();
            return info;
        }

        public static Info direct(TauDirect direct, Member otherMember) {
            Info info = new Info();
            info.chatType = ChatType.DIRECT;
            info.id = direct.id();
            info.otherMemberID = otherMember.id();
            return info;
        }
    }

    protected static class Data {
        @JsonProperty
        Collection<UUID> chats;
        @JsonProperty
        Collection<Info> infos;

        @SuppressWarnings("unused")
        public Data() {}

        public static Data get(Collection<UUID> chats) {
            Data data = new Data();
            data.chats = chats;
            return data;
        }

        public static Data infos(Collection<Info> infos) {
            Data data = new Data();
            data.infos = infos;
            return data;
        }
    }

    public ChatResponse(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CHAT), Data.class);
    }

    private ChatResponse(Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.CHAT), data);
    }

    private ChatResponse(Data data) {
        this(new Headers(), data);
    }

    public static ChatResponse get(Collection<TauChat> chats) {
        Set<UUID> chatIDs = chats
                .stream()
                .map(TauChat::id)
                .collect(Collectors.toSet());
        return new ChatResponse(Data.get(chatIDs));
    }

    public static ChatResponse infos(Collection<Info> infos) {
        return new ChatResponse(Data.infos(infos));
    }

    public Collection<UUID> getChats() {
        return object.chats;
    }

    public Collection<Info> getInfos() {
        return object.infos;
    }
}
