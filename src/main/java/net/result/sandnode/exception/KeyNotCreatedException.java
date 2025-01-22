package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class KeyNotCreatedException extends EncryptionException {
    public KeyNotCreatedException(IEncryption encryption) {
        super(encryption.name());
    }
}
