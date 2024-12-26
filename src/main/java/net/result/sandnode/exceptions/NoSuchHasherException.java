package net.result.sandnode.exceptions;

public class NoSuchHasherException extends SandnodeException {
    public NoSuchHasherException(String algorithm) {
        super(algorithm);
    }
}
