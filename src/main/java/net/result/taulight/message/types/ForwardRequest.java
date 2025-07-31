package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.TauMessageTypes;

public class ForwardRequest extends MSGPackMessage<ChatMessageInputDTO> {
    public final boolean requireDeliveryAck;

    public ForwardRequest(Headers headers, ChatMessageInputDTO input, boolean requireDeliveryAck) {
        super(headers.setType(TauMessageTypes.FWD_REQ), input);
        this.requireDeliveryAck = requireDeliveryAck;
        if (requireDeliveryAck) {
            headers().setValue("require-delivery-ack", "true");
        }
    }

    public ForwardRequest(ChatMessageInputDTO input, boolean requireDeliveryAck) {
        this(new Headers(), input, requireDeliveryAck);
    }

    public ForwardRequest(RawMessage request) throws DeserializationException, ExpectedMessageException {
        super(request.expect(TauMessageTypes.FWD_REQ), ChatMessageInputDTO.class);
        String v = headers().getValueNullable("require-delivery-ack");
        requireDeliveryAck = v != null && v.equals("true");
    }

    public ChatMessageInputDTO getChatMessageInputDTO() {
        return object;
    }
}
