package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class ServerErrorException extends SandnodeErrorException {
    public ServerErrorException() {
        super();
    }

    public ServerErrorException(Throwable e) {
        super(e);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.SERVER;
    }
}
