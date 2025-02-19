package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.TauMessageTypes;

import java.util.Collection;

public class ChatResponse extends MSGPackMessage<ChatResponse.Data> {
    protected static class Data {
        @JsonProperty
        Collection<ChatInfo> infos;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(Collection<ChatInfo> infos) {
            this.infos = infos;
        }
    }

    public ChatResponse(Collection<ChatInfo> infos) {
        super(new Headers().setType(TauMessageTypes.CHAT), new Data(infos));
    }

    public ChatResponse(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CHAT), Data.class);
    }

    public Collection<ChatInfo> getInfos() {
        return object.infos;
    }
}
