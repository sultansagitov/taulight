package net.result.sandnode.exception.crypto;

public class WrongKeyException extends CryptoException {
    public WrongKeyException(String message, Throwable e) {
        super(message, e);
    }
}
