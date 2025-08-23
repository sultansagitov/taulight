package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.TauMessageTypes;

public class ForwardResponse extends MSGPackMessage<ChatMessageViewDTO> {

    private static final String YOUR_SESSION_KEY = "your-session";

    public ForwardResponse(Headers headers, ChatMessageViewDTO message, boolean yourSession) {
        super(headers.setType(TauMessageTypes.FWD), message);
        if (yourSession) {
            headers().setValue(YOUR_SESSION_KEY, "true");
        }
    }

    public ForwardResponse(ChatMessageViewDTO message, boolean yourSession) {
        this(new Headers(), message, yourSession);
    }

    public ForwardResponse(RawMessage message) {
        super(message.expect(TauMessageTypes.FWD), ChatMessageViewDTO.class);
    }

    public ChatMessageViewDTO getServerMessage() {
        return object;
    }

    public boolean isYourSession() {
        return headers().getOptionalValue(YOUR_SESSION_KEY)
                      .map(Boolean::parseBoolean)
                      .orElse(false);
    }
}
