package net.result.taulight.message.types;

import net.result.sandnode.message.util.Headers;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.message.TauMessageTypes;

public class ForwardResponse extends MSGPackMessage<ServerChatMessage> {

    private static final String YOUR_SESSION_KEY = "your-session";

    public ForwardResponse(Headers headers, ServerChatMessage message, boolean yourSession) {
        super(headers.setType(TauMessageTypes.FWD), message);
        if (yourSession) {
            headers().setValue(YOUR_SESSION_KEY, "true");
        }
    }

    public ForwardResponse(ServerChatMessage message, boolean yourSession) {
        this(new Headers(), message, yourSession);
    }

    public ForwardResponse(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.FWD), ServerChatMessage.class);
    }

    public ServerChatMessage getServerMessage() {
        return object;
    }

    public boolean isYourSession() {
        System.out.println(headers().getOptionalValue(YOUR_SESSION_KEY));
        return headers().getOptionalValue(YOUR_SESSION_KEY)
                      .map(Boolean::parseBoolean)
                      .orElse(false);
    }
}
