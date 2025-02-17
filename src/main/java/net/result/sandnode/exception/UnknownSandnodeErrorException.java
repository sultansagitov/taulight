package net.result.sandnode.exception;

public class UnknownSandnodeErrorException extends SandnodeException {
    public UnknownSandnodeErrorException(int code) {
        super(String.valueOf(code));
    }
}
