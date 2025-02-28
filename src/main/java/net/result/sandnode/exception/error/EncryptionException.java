package net.result.sandnode.exception.error;

public class EncryptionException extends SandnodeErrorException {
    public EncryptionException(Throwable e) {
        super(e);
    }

    public EncryptionException(String message, Throwable e) {
        super(message, e);
    }

    public EncryptionException() {
        super();
    }
}
