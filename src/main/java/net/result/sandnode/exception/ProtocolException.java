package net.result.sandnode.exception;

public class ProtocolException extends SandnodeException {
    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable e) {
        super(message, e);
    }

    public ProtocolException(Throwable e) {
        super(e);
    }
}
