package net.result.taulight.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.TauMessageTypes;

import java.util.Collection;

public class ChatResponse extends MSGPackMessage<Collection<ChatInfo>> {
    public ChatResponse(Collection<ChatInfo> infos) {
        super(new Headers().setType(TauMessageTypes.CHAT), infos);
    }

    public ChatResponse(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CHAT), new TypeReference<>() {});
    }

    public Collection<ChatInfo> getInfos() {
        return object;
    }
}
