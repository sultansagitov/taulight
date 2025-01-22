package net.result.sandnode.exception;

public class NoSuchHasherException extends SandnodeException {
    public NoSuchHasherException(String algorithm) {
        super(algorithm);
    }
}
