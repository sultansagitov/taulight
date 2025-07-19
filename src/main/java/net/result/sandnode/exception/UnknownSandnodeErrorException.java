package net.result.sandnode.exception;

public class UnknownSandnodeErrorException extends ProtocolException {
    public UnknownSandnodeErrorException(String code) {
        super("Unknown error with code " + code);
    }
}
