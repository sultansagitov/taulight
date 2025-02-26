package net.result.taulight.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class DirectRequest extends Message {
    private final String memberID;

    public DirectRequest(@NotNull Headers headers, @NotNull String memberID) {
        super(headers.setType(TauMessageTypes.DIRECT));
        this.memberID = memberID;
    }

    public DirectRequest(@NotNull String memberID) {
        this(new Headers(), memberID);
    }

    public DirectRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.DIRECT).headers());
        memberID = new String(raw.getBody());
    }

    @Override
    public byte[] getBody() {
        return memberID.getBytes();
    }

    public String memberID() {
        return memberID;
    }
}
