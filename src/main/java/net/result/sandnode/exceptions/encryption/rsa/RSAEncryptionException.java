package net.result.sandnode.exceptions.encryption.rsa;

import net.result.sandnode.exceptions.encryption.EncryptionException;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;

public class RSAEncryptionException extends EncryptionException {

    public RSAEncryptionException(@NotNull GeneralSecurityException e) {
        super(e);
    }

}
