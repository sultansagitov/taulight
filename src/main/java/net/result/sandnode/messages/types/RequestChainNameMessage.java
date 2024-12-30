package net.result.sandnode.messages.types;

import net.result.sandnode.messages.util.Headers;

public class RequestChainNameMessage extends RequestMessage {
    public RequestChainNameMessage(String context) {
        super(new Headers().setValue("chain-name", context));
    }
}
