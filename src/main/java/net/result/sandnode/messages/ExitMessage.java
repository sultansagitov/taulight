package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageType.EXT;
import static net.result.sandnode.util.encryption.Encryption.NO;

public class ExitMessage extends Message implements IMessage {

    public ExitMessage(@NotNull HeadersBuilder headersBuilder) {
        super(headersBuilder.set(NO).set(EXT));
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }

}
