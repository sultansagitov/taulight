package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class WrongEncryptionException extends SandnodeException {
    public WrongEncryptionException(IEncryption encryption) {
        super(encryption.name());
    }
}
