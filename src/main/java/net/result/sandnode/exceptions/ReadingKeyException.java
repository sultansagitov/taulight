package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

public class ReadingKeyException extends Exception {
    public ReadingKeyException(@NotNull String message) {
        super(message);
    }

    public ReadingKeyException(@NotNull Exception e) {
        super(e);
    }
}
