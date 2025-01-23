package net.result.sandnode.exception;

import java.io.IOException;
import java.nio.file.Path;

public class FSException extends SandnodeException {
    public FSException(String message) {
        super(message);
    }

    public FSException(Exception e) {
        super(e);
    }

    public FSException(String message, Exception e) {
        super(message, e);
    }

    public FSException(String message, Path path) {
        this("%s: %s".formatted(message, path));
    }

    public FSException(String message, Path path, IOException e) {
        this("%s: %s".formatted(message, path), e);
    }
}
