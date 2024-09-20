package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

import java.security.spec.InvalidKeySpecException;

public class CreatingKeyException extends SandnodeException {
    public CreatingKeyException(@NotNull String message) {
        super(message);
    }

    public CreatingKeyException(@NotNull InvalidKeySpecException e) {
        super(e);
    }
}
