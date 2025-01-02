package net.result.taulight.messages.types;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class ForwardMessage extends TextMessage {
    public ForwardMessage(Headers headers, String data) {
        super(headers.setType(FWD), data);
    }

    public ForwardMessage(String data) {
        this(new Headers(), data);
    }

    public ForwardMessage(IMessage request) {
        super(request);
    }
}
