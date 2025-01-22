package net.result.sandnode.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.StatusMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;

import static net.result.sandnode.message.util.MessageTypes.ERR;

public class ErrorMessage extends StatusMessage {
    public final SandnodeError error;

    public ErrorMessage(SandnodeError serverError) {
        super(new Headers().setType(ERR), serverError.getCode());
        this.error = serverError;
    }

    public ErrorMessage(IMessage response) throws DeserializationException {
        super(response);

        for (SandnodeError error : ServerErrorManager.instance().list) {
            if (error.getCode() == this.getCode()) {
                this.error = error;
                return;
            }
        }

        throw new UnknownSandnodeErrorException();
    }
}
