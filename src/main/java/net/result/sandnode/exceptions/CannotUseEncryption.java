package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class CannotUseEncryption extends SandnodeException {

    public CannotUseEncryption(IEncryption encryption) {
        super(encryption.name());
    }

    public CannotUseEncryption(String encryption) {
        super(encryption);
    }
}
