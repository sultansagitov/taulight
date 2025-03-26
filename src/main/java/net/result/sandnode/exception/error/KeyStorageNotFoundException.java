package net.result.sandnode.exception.error;

import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import org.jetbrains.annotations.NotNull;

public class KeyStorageNotFoundException extends SandnodeErrorException {
    public KeyStorageNotFoundException(@NotNull Encryption encryption) {
        super(encryption.name());
    }

    public KeyStorageNotFoundException() {
        super();
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.KEY_NOT_FOUND;
    }
}
