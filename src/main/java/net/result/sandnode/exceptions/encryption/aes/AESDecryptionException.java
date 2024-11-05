package net.result.sandnode.exceptions.encryption.aes;

import net.result.sandnode.exceptions.encryption.DecryptionException;
import org.jetbrains.annotations.NotNull;

public class AESDecryptionException extends DecryptionException {

    public AESDecryptionException(@NotNull Exception e) {
        super(e);
    }

}
