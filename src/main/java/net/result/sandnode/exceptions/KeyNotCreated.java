package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.interfaces.IEncryption;
import org.jetbrains.annotations.NotNull;

public class KeyNotCreated extends SandnodeException {
    public KeyNotCreated(@NotNull IEncryption encryption) {
        super(encryption.name());
    }
}
