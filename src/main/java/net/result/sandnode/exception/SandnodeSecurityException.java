package net.result.sandnode.exception;

public class SandnodeSecurityException extends SandnodeException {
    public SandnodeSecurityException(String message) {
        super(message);
    }

    public SandnodeSecurityException(String message, Throwable e) {
        super(message, e);
    }
}
