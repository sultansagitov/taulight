package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class ServerSandnodeErrorException extends SandnodeErrorException {
    public ServerSandnodeErrorException() {
        super();
    }

    public ServerSandnodeErrorException(Throwable e) {
        super(e);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.SERVER;
    }
}
