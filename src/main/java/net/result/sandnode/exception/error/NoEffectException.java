package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class NoEffectException extends SandnodeErrorException {
    public NoEffectException() {
    }

    public NoEffectException(String message) {
        super(message);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.NO_EFFECT;
    }
}
