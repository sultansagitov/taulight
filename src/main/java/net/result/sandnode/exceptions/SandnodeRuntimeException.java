package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

public class SandnodeRuntimeException extends RuntimeException {
    public SandnodeRuntimeException(@NotNull String message) {
        super(message);
    }

    public SandnodeRuntimeException(@NotNull Exception e) {
        super(e);
    }
}
