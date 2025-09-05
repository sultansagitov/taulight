package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.TauMessageTypes;

public class UpstreamRequest extends MSGPackMessage<ChatMessageInputDTO> {
    private static final String REQUIRE_DELIVERY_ACK_KEY = "require-delivery-ack";

    public final boolean requireDeliveryAck;

    public UpstreamRequest(Headers headers, ChatMessageInputDTO input, boolean requireDeliveryAck) {
        super(headers.setType(TauMessageTypes.UPSTREAM), input);
        this.requireDeliveryAck = requireDeliveryAck;
        if (requireDeliveryAck) {
            headers().setValue(REQUIRE_DELIVERY_ACK_KEY, "true");
        }
    }

    public UpstreamRequest(ChatMessageInputDTO input, boolean requireDeliveryAck) {
        this(new Headers(), input, requireDeliveryAck);
    }

    public UpstreamRequest(RawMessage raw) {
        super(raw.expect(TauMessageTypes.UPSTREAM), ChatMessageInputDTO.class);
        String v = headers().getValueNullable(REQUIRE_DELIVERY_ACK_KEY);
        requireDeliveryAck = v != null && v.equals("true");
    }

    public ChatMessageInputDTO getChatMessageInputDTO() {
        return object;
    }
}
