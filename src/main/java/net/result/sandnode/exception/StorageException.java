package net.result.sandnode.exception;

public class StorageException extends SandnodeException {
    public StorageException(Throwable e) {
        super(e);
    }

    public StorageException(String message, Throwable e) {
        super(message, e);
    }
}
