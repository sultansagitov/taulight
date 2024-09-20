package net.result.sandnode.exceptions.encryption.aes;

import net.result.sandnode.exceptions.encryption.EncryptionException;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;

public class AESEncryptionException extends EncryptionException {

    public AESEncryptionException(@NotNull String message) {
        super(message);
    }

    public AESEncryptionException(@NotNull GeneralSecurityException e) {
        super(e);
    }

}
