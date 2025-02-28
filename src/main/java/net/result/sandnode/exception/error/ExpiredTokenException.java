package net.result.sandnode.exception.error;

public class ExpiredTokenException extends SandnodeErrorException {
    public ExpiredTokenException(Throwable e) {
        super(e);
    }

    public ExpiredTokenException() {
        super();
    }
}
