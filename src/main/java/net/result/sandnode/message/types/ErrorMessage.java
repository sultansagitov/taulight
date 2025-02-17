package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.StatusMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.MessageTypes.ERR;

public class ErrorMessage extends StatusMessage {
    public final SandnodeError error;

    public ErrorMessage(@NotNull SandnodeError serverError) {
        super(new Headers().setType(ERR), serverError.code());
        this.error = serverError;
    }

    public ErrorMessage(@NotNull IMessage response) throws ExpectedMessageException, UnknownSandnodeErrorException {
        super(response.expect(ERR));

        for (SandnodeError error : ServerErrorManager.instance().list) {
            if (error.code() == code()) {
                this.error = error;
                return;
            }
        }

        throw new UnknownSandnodeErrorException(code());
    }
}
