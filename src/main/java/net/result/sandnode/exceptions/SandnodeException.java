package net.result.sandnode.exceptions;

public class SandnodeException extends Exception {
    public SandnodeException(String message) {
        super(message);
    }

    public SandnodeException(Throwable e) {
        super(e);
    }

    public SandnodeException(String message, Throwable e) {
        super(message, e);
    }

    public SandnodeException() {
    }
}
