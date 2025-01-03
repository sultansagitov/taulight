package net.result.sandnode.messages.types;

import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.util.Headers;

import static net.result.sandnode.messages.util.MessageTypes.PUB;

public class PublicKeyRequest extends EmptyMessage {
    public PublicKeyRequest() {
        super(new Headers().setType(PUB));
    }
}
