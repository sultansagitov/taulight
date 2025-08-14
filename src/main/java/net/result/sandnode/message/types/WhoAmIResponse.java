package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.BaseMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class WhoAmIResponse extends BaseMessage {
    private final String nickname;

    public WhoAmIResponse(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.WHOAMI).headers());
        nickname = new String(raw.getBody());
    }

    public WhoAmIResponse(String nickname) {
        super(new Headers().setType(MessageTypes.WHOAMI));
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public byte[] getBody() {
        return nickname.getBytes();
    }
}
