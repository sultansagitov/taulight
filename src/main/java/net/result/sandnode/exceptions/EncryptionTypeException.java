package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class EncryptionTypeException extends SandnodeException {
    public EncryptionTypeException(IEncryption encryption) {
        super(encryption.name());
    }
}
