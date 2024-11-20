package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.interfaces.IEncryption;
import org.jetbrains.annotations.NotNull;

public class KeyStorageNotFoundException extends SandnodeException {
    public KeyStorageNotFoundException(@NotNull IEncryption encryption) {
        super(encryption.name());
    }
}
