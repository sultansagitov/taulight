package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.Encryption;

public class EncryptionTypeException extends EncryptionException {
    public EncryptionTypeException(Encryption encryption) {
        super(encryption);
    }
}
