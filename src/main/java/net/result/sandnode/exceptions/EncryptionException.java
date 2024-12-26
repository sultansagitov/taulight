package net.result.sandnode.exceptions;

public class EncryptionException extends SandnodeException {
    public EncryptionException(Throwable e) {
        super(e);
    }

    public EncryptionException(String message, Throwable e) {
        super(message, e);
    }
}
