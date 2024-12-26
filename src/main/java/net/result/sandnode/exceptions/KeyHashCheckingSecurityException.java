package net.result.sandnode.exceptions;

public class KeyHashCheckingSecurityException extends SandnodeSecurityException {
    public KeyHashCheckingSecurityException(String hash, String expected) {
        super("Got \"%s\", instead of \"%s\"".formatted(hash, expected));
    }
}
