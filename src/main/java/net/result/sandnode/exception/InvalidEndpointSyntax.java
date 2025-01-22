package net.result.sandnode.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidEndpointSyntax extends SandnodeException {
    public InvalidEndpointSyntax(Throwable e) {
        super(e);
    }

    public InvalidEndpointSyntax(@NotNull String input, String message) {
        super("%s: %s".formatted(input, message));
    }
}
