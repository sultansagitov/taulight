package net.result.sandnode.exceptions;

public class SocketClosingException extends SandnodeException {
    public SocketClosingException(String message, Throwable e) {
        super(message, e);
    }
}
