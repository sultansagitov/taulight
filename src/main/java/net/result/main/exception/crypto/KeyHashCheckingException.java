package net.result.main.exception.crypto;

import net.result.sandnode.exception.crypto.CryptoException;

public class KeyHashCheckingException extends CryptoException {
    public KeyHashCheckingException(String hash, String expected) {
        super("Got \"%s\", instead of \"%s\"".formatted(hash, expected));
    }
}
