package net.result.sandnode.exception;

public class WrongKeyException extends EncryptionException {
    public WrongKeyException(String message, Throwable e) {
        super(message, e);
    }
}
