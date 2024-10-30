package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

public class KeyStorageNotFoundException extends RuntimeException {
    public KeyStorageNotFoundException(String message) {
        super(message);
    }

    public KeyStorageNotFoundException(@NotNull Encryption encryption) {
        super(encryption.name());
    }
}
