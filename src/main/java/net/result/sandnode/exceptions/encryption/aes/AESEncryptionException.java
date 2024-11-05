package net.result.sandnode.exceptions.encryption.aes;

import net.result.sandnode.exceptions.encryption.EncryptionException;
import org.jetbrains.annotations.NotNull;

public class AESEncryptionException extends EncryptionException {

    public AESEncryptionException(@NotNull Exception e) {
        super(e);
    }

}
