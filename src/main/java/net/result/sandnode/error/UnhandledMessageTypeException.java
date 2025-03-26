package net.result.sandnode.error;

import net.result.sandnode.exception.error.SandnodeErrorException;

public class UnhandledMessageTypeException extends SandnodeErrorException {
    @Override
    public SandnodeError getSandnodeError() {
        return Errors.UNHANDLED_MESSAGE_TYPE;
    }
}
