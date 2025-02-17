package net.result.sandnode.exception;

public class ExpiredTokenException extends SandnodeErrorException {
    public ExpiredTokenException(Throwable e) {
        super(e);
    }

    public ExpiredTokenException() {
        super();
    }
}
