package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class EncryptionException extends SandnodeErrorException {
    public EncryptionException(Throwable e) {
        super(e);
    }

    public EncryptionException(String message, Throwable e) {
        super(message, e);
    }

    public EncryptionException() {
        super();
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.ENCRYPT;
    }

}
