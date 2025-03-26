package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class TooFewArgumentsException extends SandnodeErrorException {
    @Override
    public SandnodeError getSandnodeError() {
        return Errors.TOO_FEW_ARGS;
    }
}
