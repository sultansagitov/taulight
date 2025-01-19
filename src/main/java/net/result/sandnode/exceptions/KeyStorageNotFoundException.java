package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class KeyStorageNotFoundException extends EncryptionException {
    public KeyStorageNotFoundException(IEncryption encryption) {
        super(encryption.name());
    }
}
