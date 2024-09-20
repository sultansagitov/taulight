package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

public class NoSuchServerErrorException extends SandnodeRuntimeException {
    public NoSuchServerErrorException(@NotNull String message) {
        super(message);
    }

    public NoSuchServerErrorException(@NotNull Exception e) {
        super(e);
    }
}
