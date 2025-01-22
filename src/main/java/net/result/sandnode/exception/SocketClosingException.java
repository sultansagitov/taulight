package net.result.sandnode.exception;

public class SocketClosingException extends SandnodeException {
    public SocketClosingException(String message, Throwable e) {
        super(message, e);
    }
}
