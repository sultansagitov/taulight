package net.result.sandnode.exception.error;

import net.result.sandnode.exception.SandnodeException;

public class SandnodeErrorException extends SandnodeException {
    public SandnodeErrorException() {
        super();
    }

    public SandnodeErrorException(Throwable e) {
        super(e);
    }

    public SandnodeErrorException(String message, Throwable e) {
        super(message, e);
    }

    public SandnodeErrorException(String message) {
        super(message);
    }
}
