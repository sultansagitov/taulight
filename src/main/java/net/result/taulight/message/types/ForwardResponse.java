package net.result.taulight.message.types;

import net.result.sandnode.message.util.Headers;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.message.TauMessageTypes;

public class ForwardResponse extends MSGPackMessage<ServerChatMessage> {

    public ForwardResponse(Headers headers, ServerChatMessage message) {
        super(headers.setType(TauMessageTypes.FWD), message);
    }

    public ForwardResponse(ServerChatMessage message) {
        this(new Headers(), message);
    }

    public ForwardResponse(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.FWD), ServerChatMessage.class);
    }

    public ServerChatMessage getServerMessage() {
        return object;
    }
}
