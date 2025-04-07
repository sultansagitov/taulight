package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.TauMessageTypes;

public class ForwardRequest extends MSGPackMessage<ChatMessageInputDTO> {
    public ForwardRequest(ChatMessageInputDTO chatMessage) {
        this(new Headers(), chatMessage);
    }

    public ForwardRequest(Headers headers, ChatMessageInputDTO chatMessage) {
        super(headers.setType(TauMessageTypes.FWD_REQ), chatMessage);
    }

    public ForwardRequest(RawMessage request) throws DeserializationException, ExpectedMessageException {
        super(request.expect(TauMessageTypes.FWD_REQ), ChatMessageInputDTO.class);
    }

    public ChatMessageInputDTO getChatMessage() {
        return object;
    }
}
