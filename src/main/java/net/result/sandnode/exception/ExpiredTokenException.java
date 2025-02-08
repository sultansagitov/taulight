package net.result.sandnode.exception;

public class ExpiredTokenException extends SandnodeSecurityException {
    public ExpiredTokenException(Throwable e) {
        super(e);
    }

    public ExpiredTokenException() {
        super();
    }
}
