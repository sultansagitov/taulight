package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.StatusMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class ErrorMessage extends StatusMessage {
    public final SandnodeError error;

    public ErrorMessage(@NotNull SandnodeError serverError) {
        super(new Headers().setType(MessageTypes.ERR), serverError.code());
        this.error = serverError;
    }

    public ErrorMessage(@NotNull RawMessage response) throws ExpectedMessageException, UnknownSandnodeErrorException {
        super(response.expect(MessageTypes.ERR));

        for (SandnodeError error : ServerErrorManager.instance().list) {
            if (error.code() == code()) {
                this.error = error;
                return;
            }
        }

        throw new UnknownSandnodeErrorException(code());
    }

    @Override
    public String toString() {
        return "%s %s".formatted(super.toString(), error.description());
    }
}
