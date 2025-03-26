package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class DecryptionException extends SandnodeErrorException {
    public DecryptionException() {
        super();
    }

    public DecryptionException(String message, Throwable e) {
        super(message, e);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.DECRYPT;
    }
}
