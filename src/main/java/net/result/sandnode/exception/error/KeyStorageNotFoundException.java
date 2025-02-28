package net.result.sandnode.exception.error;

import net.result.sandnode.encryption.interfaces.Encryption;
import org.jetbrains.annotations.NotNull;

public class KeyStorageNotFoundException extends SandnodeErrorException {
    public KeyStorageNotFoundException(@NotNull Encryption encryption) {
        super(encryption.name());
    }

    public KeyStorageNotFoundException() {
        super();
    }
}
