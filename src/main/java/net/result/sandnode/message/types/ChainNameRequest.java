package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;

public class ChainNameRequest extends EmptyMessage {
    public ChainNameRequest(String chainName) {
        super(new Headers().setType(MessageTypes.CHAIN_NAME).setValue("chain-name", chainName));
    }
}
