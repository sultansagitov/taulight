package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;

public class EncryptionException extends SandnodeException {

    public EncryptionException(@NotNull String message) {
        super(message);
    }

    public EncryptionException(@NotNull GeneralSecurityException e) {
        super(e);
    }

}
