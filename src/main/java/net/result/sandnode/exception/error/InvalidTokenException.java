package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class InvalidTokenException extends SandnodeErrorException {
    public InvalidTokenException() {
        super();
    }

    public InvalidTokenException(Throwable e) {
        super(e);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.INVALID_TOKEN;
    }
}
