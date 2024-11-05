package net.result.sandnode.exceptions.encryption.rsa;

import net.result.sandnode.exceptions.encryption.DecryptionException;
import org.jetbrains.annotations.NotNull;

import java.security.GeneralSecurityException;

public class RSADecryptionException extends DecryptionException {

    public RSADecryptionException(@NotNull GeneralSecurityException e) {
        super(e);
    }

}
