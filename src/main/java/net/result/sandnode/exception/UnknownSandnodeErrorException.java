package net.result.sandnode.exception;

public class UnknownSandnodeErrorException extends SandnodeException {
    public UnknownSandnodeErrorException(String code) {
        super("Unknown error with code " + code);
    }
}
