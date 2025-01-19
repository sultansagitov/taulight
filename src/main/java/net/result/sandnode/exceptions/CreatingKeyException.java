package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class CreatingKeyException extends EncryptionException {
    public CreatingKeyException(IEncryption encryption, Throwable e) {
        super(encryption.name(), e);
    }
}
