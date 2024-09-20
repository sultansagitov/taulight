package net.result.sandnode.exceptions.encryption.aes;

import net.result.sandnode.exceptions.encryption.DecryptionException;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;

public class AESDecryptionException extends DecryptionException {

    public AESDecryptionException(@NotNull String message) {
        super(message);
    }

    public AESDecryptionException(@NotNull GeneralSecurityException e) {
        super(e);
    }

}
