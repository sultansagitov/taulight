package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

public class KeyNotCreated extends SandnodeException {
    public KeyNotCreated(@NotNull Encryption encryption) {
        super(encryption.name());
    }
}
