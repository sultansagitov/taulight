package net.result.sandnode.exceptions;

public class ImpossibleRuntimeException extends RuntimeException {
    public ImpossibleRuntimeException(Exception e) {
        super(e);
    }

    public ImpossibleRuntimeException(String message) {
        super(message);
    }
}
