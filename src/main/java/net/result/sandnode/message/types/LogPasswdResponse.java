package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LogPasswdResponse extends TextMessage {
    public LogPasswdResponse(@NotNull Headers headers, String token) {
        super(headers.setType(MessageTypes.LOG_PASSWD), token);
    }

    public LogPasswdResponse(String token) {
        this(new Headers(), token);
    }

    public LogPasswdResponse(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.LOG_PASSWD));
    }
}
