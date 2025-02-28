package net.result.sandnode.exception.crypto;

import net.result.sandnode.encryption.interfaces.Encryption;

public class EncryptionTypeException extends CryptoException {
    public EncryptionTypeException(Encryption encryption) {
        super(encryption);
    }
}
