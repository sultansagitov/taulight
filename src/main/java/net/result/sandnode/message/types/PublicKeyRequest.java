package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;

import static net.result.sandnode.message.util.MessageTypes.PUB;

public class PublicKeyRequest extends EmptyMessage {
    public PublicKeyRequest() {
        super(new Headers().setType(PUB));
    }
}
