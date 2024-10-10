package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import org.jetbrains.annotations.NotNull;

public class NoSuchEncryptionException extends SandnodeException {
    public NoSuchEncryptionException(@NotNull String message) {
        super(message);
    }

    public NoSuchEncryptionException(byte message) {
        super("No such encryption for " + message);
    }

}
