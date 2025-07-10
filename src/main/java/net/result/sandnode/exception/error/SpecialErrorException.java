package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class SpecialErrorException extends SandnodeErrorException {
    public final String special;

    public SpecialErrorException(String special) {
        super();
        this.special = special;
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.SPECIAL;
    }
}
