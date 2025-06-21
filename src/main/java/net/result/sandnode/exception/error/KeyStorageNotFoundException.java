package net.result.sandnode.exception.error;

import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class KeyStorageNotFoundException extends SandnodeErrorException {
    public KeyStorageNotFoundException(@NotNull Encryption encryption) {
        super(encryption.name());
    }

    public KeyStorageNotFoundException(UUID keyID) {
        super(keyID.toString());
    }

    public KeyStorageNotFoundException(String message) {
        super(message);
    }

    public KeyStorageNotFoundException(Throwable e) {
        super(e);
    }

    public KeyStorageNotFoundException() {
        super();
    }

    @Override
    public SandnodeError getSandnodeError() {
        return Errors.KEY_NOT_FOUND;
    }
}
