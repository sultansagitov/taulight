package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class InvalidArgumentException extends SandnodeErrorException {
    public InvalidArgumentException() {
        super();
    }

    public InvalidArgumentException(Throwable e) {
        super(e);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.INVALID_ARG;
    }
}
