package net.result.sandnode.messages.types;

import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.util.Headers;

import static net.result.sandnode.messages.util.MessageTypes.CHAIN_NAME;

public class RequestChainNameMessage extends EmptyMessage {
    public RequestChainNameMessage(String context) {
        super(new Headers().setType(CHAIN_NAME).setValue("chain-name", context));
    }
}
