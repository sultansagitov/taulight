package net.result.sandnode.exceptions;

public class DecryptionException extends SandnodeException {
    public DecryptionException(Throwable e) {
        super(e);
    }

    public DecryptionException(String message, Throwable e) {
        super(message, e);
    }
}
