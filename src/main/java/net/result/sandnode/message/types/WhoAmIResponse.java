package net.result.sandnode.message.types;

import net.result.sandnode.db.Member;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class WhoAmIResponse extends Message {
    private final String id;

    public WhoAmIResponse(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.WHOAMI).headers());
        id = new String(raw.getBody());
    }

    public WhoAmIResponse(@NotNull Member member) {
        super(new Headers().setType(MessageTypes.WHOAMI));
        id = member.id();
    }

    public String getID() {
        return id;
    }

    @Override
    public byte[] getBody() {
        return id.getBytes();
    }
}
