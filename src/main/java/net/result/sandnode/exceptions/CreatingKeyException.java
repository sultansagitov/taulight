package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

import java.security.spec.InvalidKeySpecException;

public class CreatingKeyException extends SandnodeException {

    public CreatingKeyException(@NotNull InvalidKeySpecException e) {
        super(e);
    }

    public CreatingKeyException(@NotNull Encryption encryption, @NotNull Exception e) {
        super(encryption.name(), e);
    }

}
