package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class ExpiredTokenException extends SandnodeErrorException {
    public ExpiredTokenException(Throwable e) {
        super(e);
    }

    public ExpiredTokenException() {
        super();
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.EXPIRED_TOKEN;
    }
}
