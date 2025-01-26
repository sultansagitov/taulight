package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.Encryption;

public class CreatingKeyException extends EncryptionException {
    public CreatingKeyException(Encryption encryption, Throwable e) {
        super(encryption.name(), e);
    }
}
