package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;

public class DecryptionException extends SandnodeException {

    public DecryptionException(@NotNull String message) {
        super(message);
    }

    public DecryptionException(@NotNull GeneralSecurityException e) {
        super(e);
    }

}
