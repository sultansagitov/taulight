package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.interfaces.IEncryption;

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

    public EncryptionException(IEncryption encryption) {
        super(encryption.name());
    }
}
