package net.result.sandnode.exception.crypto;

import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.exception.SandnodeSecurityException;

public class CryptoException extends SandnodeSecurityException {
    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable e) {
        super(message, e);
    }

    public CryptoException(Encryption encryption) {
        super(encryption.name());
    }
}
