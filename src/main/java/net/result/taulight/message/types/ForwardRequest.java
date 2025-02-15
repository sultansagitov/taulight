package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.message.TauMessageTypes;

public class ForwardRequest extends MSGPackMessage<ChatMessage> {
    public ForwardRequest(ChatMessage chatMessage) {
        this(new Headers(), chatMessage);
    }

    public ForwardRequest(Headers headers, ChatMessage chatMessage) {
        super(headers.setType(TauMessageTypes.FWD_REQ), chatMessage);
    }

    public ForwardRequest(IMessage request) throws DeserializationException, ExpectedMessageException {
        super(request.expect(TauMessageTypes.FWD_REQ), ChatMessage.class);
    }

    public ChatMessage getChatMessage() {
        return object;
    }
}
