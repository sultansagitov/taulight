package net.result.sandnode.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidAddressSyntax extends SandnodeException {
    public InvalidAddressSyntax(Throwable e) {
        super(e);
    }

    public InvalidAddressSyntax(@NotNull String input, String message) {
        super("%s: %s".formatted(input, message));
    }
}
