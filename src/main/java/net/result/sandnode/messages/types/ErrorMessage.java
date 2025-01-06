package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.UnknownSandnodeErrorException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.StatusMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.server.ServerError;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

public class ErrorMessage extends StatusMessage {
    public final ServerError error;

    public ErrorMessage(ServerError serverError) {
        super(new Headers().setType(ERR), serverError.code);
        this.error = serverError;
    }

    public ErrorMessage(IMessage response) throws DeserializationException {
        super(response);

        for (ServerError error : ServerError.values()) {
            if (error.code == this.getCode()) {
                this.error = error;
                return;
            }
        }

        throw new UnknownSandnodeErrorException();
    }
}
