package net.result.sandnode.exceptions;

public class InvalidSandnodeLinkException extends SandnodeException {

    public InvalidSandnodeLinkException(String message) {
        super(message);
    }

    public InvalidSandnodeLinkException(String message, Throwable e) {
        super(message, e);
    }

    public InvalidSandnodeLinkException(Throwable e) {
        super(e);
    }

}
