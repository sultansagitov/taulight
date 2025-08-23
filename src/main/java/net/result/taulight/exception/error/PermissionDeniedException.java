package net.result.taulight.exception.error;

import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.taulight.error.TauErrors;

public class PermissionDeniedException extends SandnodeErrorException {
    public PermissionDeniedException() {}

    public PermissionDeniedException(String message) {
        super(message);
    }

    @Override
    public SandnodeError getSandnodeError() {
        return TauErrors.PERMISSION_DENIED;
    }
}
