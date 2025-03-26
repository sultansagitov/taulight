package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class UnknownEncryptionException extends SandnodeErrorException {
    @Override
    public SandnodeError getSandnodeError() {
        return Errors.UNKNOWN_ENCRYPTION;
    }
}
