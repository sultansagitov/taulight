package net.result.sandnode.exceptions;

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
        super(String.format("%s: %s", message, path));
    }
}
