package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

public class SandnodeException extends Exception {
    public SandnodeException(@NotNull String message) {
        super(message);
    }

    public SandnodeException(@NotNull Exception e) {
        super(e);
    }
}
