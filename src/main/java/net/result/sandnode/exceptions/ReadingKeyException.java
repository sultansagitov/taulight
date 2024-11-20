package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

public class ReadingKeyException extends SandnodeException {

    public ReadingKeyException(@NotNull Exception e) {
        super(e);
    }

    public ReadingKeyException(@NotNull String message) {
        super(message);
    }

}
