package net.result.sandnode.exception;

import net.result.sandnode.error.SandnodeError;

public class ImpossibleRuntimeException extends RuntimeException {
    public ImpossibleRuntimeException(Exception e) {
        super(e);
    }

    public ImpossibleRuntimeException(String message) {
        super(message);
    }

    public ImpossibleRuntimeException(SandnodeError error) {
        super("Error from another node: code %s, %s".formatted(error.code(), error.description()));
    }
}
