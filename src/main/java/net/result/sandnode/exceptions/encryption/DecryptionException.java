package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import org.jetbrains.annotations.NotNull;

public class DecryptionException extends SandnodeException {

    public DecryptionException(@NotNull Exception e) {
        super(e);
    }

}
