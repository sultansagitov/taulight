package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.UnknownSandnodeErrorException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.StatusMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.server.ServerErrorInterface;
import net.result.sandnode.server.ServerErrorManager;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

public class ErrorMessage extends StatusMessage {
    public final ServerErrorInterface error;

    public ErrorMessage(ServerErrorInterface serverError) {
        super(new Headers().setType(ERR), serverError.getCode());
        this.error = serverError;
    }

    public ErrorMessage(IMessage response) throws DeserializationException {
        super(response);

        for (ServerErrorInterface error : ServerErrorManager.instance().list) {
            if (error.getCode() == this.getCode()) {
                this.error = error;
                return;
            }
        }

        throw new UnknownSandnodeErrorException();
    }
}
