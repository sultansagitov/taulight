package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.Encryption;

public class KeyStorageNotFoundException extends EncryptionException {
    public KeyStorageNotFoundException(Encryption encryption) {
        super(encryption.name());
    }
}
