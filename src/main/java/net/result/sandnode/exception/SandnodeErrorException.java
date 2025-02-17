package net.result.sandnode.exception;

import net.result.sandnode.error.SandnodeError;
import org.jetbrains.annotations.NotNull;

public class SandnodeErrorException extends SandnodeException {
    public SandnodeErrorException() {
        super();
    }

    public SandnodeErrorException(String message) {
        super(message);
    }

    public SandnodeErrorException(@NotNull SandnodeError error) {
        super("code: %d - %s".formatted(error.code(), error.description()));
    }

    public SandnodeErrorException(Throwable e) {
        super(e);
    }
}
