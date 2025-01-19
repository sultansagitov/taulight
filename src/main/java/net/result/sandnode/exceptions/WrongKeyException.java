package net.result.sandnode.exceptions;

public class WrongKeyException extends EncryptionException {
    public WrongKeyException(String message, Throwable e) {
        super(message, e);
    }
}
