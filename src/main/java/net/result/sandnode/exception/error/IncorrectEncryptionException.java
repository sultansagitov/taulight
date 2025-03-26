package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class IncorrectEncryptionException extends SandnodeErrorException {
    @Override
    public SandnodeError getSandnodeError() {
        return Errors.INCORRECT_ENCRYPTION;
    }
}
