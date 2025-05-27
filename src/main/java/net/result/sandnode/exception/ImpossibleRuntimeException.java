package net.result.sandnode.exception;

public class ImpossibleRuntimeException extends RuntimeException {

    public ImpossibleRuntimeException(Throwable e) {
        super(e);
    }

    public ImpossibleRuntimeException(String message) {
        super(message);
    }

}
