package net.result.sandnode.exception;

public class DatabaseException extends SandnodeException {
    public DatabaseException(String message, Throwable e) {
        super(message, e);
    }

    // TODO remove this
    public DatabaseException(Exception e) {
        super(e);
    }
}
