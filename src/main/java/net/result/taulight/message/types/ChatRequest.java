package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class ChatRequest extends MSGPackMessage<ChatRequest.Data> {
    protected static class Data {
        @JsonProperty("chat-id-list")
        public @Nullable Collection<UUID> allChatID = null;
        @JsonProperty("properties")
        private Collection<ChatInfoPropDTO> infoProps;

        @SuppressWarnings("unused")
        public Data() {
        }

        public Data(Collection<ChatInfoPropDTO> infoProps) {
            this.infoProps = infoProps;
        }

        public Data(@NotNull Collection<UUID> allChatID, Collection<ChatInfoPropDTO> infoProps) {
            this.allChatID = allChatID;
            this.infoProps = infoProps;
        }
    }

    private ChatRequest(Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.CHAT), data);
    }

    private ChatRequest(Data data) {
        this(new Headers(), data);
    }

    public ChatRequest(RawMessage raw) throws DeserializationException {
        super(raw, Data.class);
    }

    public static ChatRequest getByMember(Collection<ChatInfoPropDTO> infoProps) {
        return new ChatRequest(new Data(infoProps));
    }

    public static ChatRequest getByID(Collection<UUID> chatID, Collection<ChatInfoPropDTO> infoProps) {
        return new ChatRequest(new Data(chatID, infoProps));
    }

    public Collection<UUID> getAllChatID() {
        return object.allChatID;
    }

    public Collection<ChatInfoPropDTO> getChatInfoProps() {
        return object.infoProps;
    }
}