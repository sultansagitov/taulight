package net.result.taulight.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class DialogRequest extends Message {
    private final String memberID;

    public DialogRequest(@NotNull Headers headers, @NotNull String memberID) {
        super(headers.setType(TauMessageTypes.DIALOG));
        this.memberID = memberID;
    }

    public DialogRequest(@NotNull String memberID) {
        this(new Headers(), memberID);
    }

    public DialogRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.DIALOG).headers());
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
