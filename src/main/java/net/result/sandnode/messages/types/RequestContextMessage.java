package net.result.sandnode.messages.types;

import net.result.sandnode.messages.util.Headers;

public class RequestContextMessage extends RequestMessage {
    public RequestContextMessage(String context) {
        super(new Headers().setValue("chain-name", context));
    }
}
