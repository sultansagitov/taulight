package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.Encryption;

public class EncryptionException extends SandnodeException {
    public EncryptionException(Throwable e) {
        super(e);
    }

    public EncryptionException(String message, Throwable e) {
        super(message, e);
    }

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(Encryption encryption) {
        super(encryption.name());
    }
}
