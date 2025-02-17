package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;

public class PublicKeyRequest extends EmptyMessage {
    public PublicKeyRequest() {
        super(new Headers().setType(MessageTypes.PUB));
    }
}
