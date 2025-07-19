package net.result.sandnode.exception.error;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;

public class SpecialErrorException extends SandnodeErrorException {
    public final String special;

    public SpecialErrorException(String special) {
        super();
        this.special = special;
    }

    public SpecialErrorException(String special, String message) {
        super(message);
        this.special = special;
    }

    public SpecialErrorException(String special, String message, Throwable e) {
        super(message, e);
        this.special = special;
    }

    public SpecialErrorException(String special, Throwable e) {
        super(e);
        this.special = special;
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.SPECIAL;
    }

    @Override
    public String toString() {
        return "%s %s".formatted(super.toString(), special);
    }
}
