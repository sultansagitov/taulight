package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import org.jetbrains.annotations.NotNull;

public class EncryptionException extends SandnodeException {

    public EncryptionException(@NotNull Exception e) {
        super(e);
    }

}
