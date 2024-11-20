package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.interfaces.IEncryption;
import org.jetbrains.annotations.NotNull;

public class CreatingKeyException extends SandnodeException {

    public CreatingKeyException(@NotNull IEncryption encryption, @NotNull Exception e) {
        super(encryption.name(), e);
    }

}
