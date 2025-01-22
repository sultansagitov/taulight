package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.IEncryption;

public class CannotUseEncryption extends SandnodeException {
    public CannotUseEncryption(IEncryption encryption) {
        super(encryption.name());
    }

    public CannotUseEncryption(IEncryption encryption, IEncryption expected) {
        super("Got %s, instead of %s".formatted(encryption, expected));
    }
}
