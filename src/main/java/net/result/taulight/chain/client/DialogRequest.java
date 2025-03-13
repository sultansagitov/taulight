package net.result.taulight.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class DialogRequest extends Message {
    private final String nickname;

    public DialogRequest(@NotNull Headers headers, @NotNull String nickname) {
        super(headers.setType(TauMessageTypes.DIALOG));
        this.nickname = nickname;
    }

    public DialogRequest(@NotNull String nickname) {
        this(new Headers(), nickname);
    }

    public DialogRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.DIALOG).headers());
        nickname = new String(raw.getBody());
    }

    @Override
    public byte[] getBody() {
        return nickname.getBytes();
    }

    public String nickname() {
        return nickname;
    }
}
