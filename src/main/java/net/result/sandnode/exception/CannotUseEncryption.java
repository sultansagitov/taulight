package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.Encryption;

public class CannotUseEncryption extends SandnodeException {
    public CannotUseEncryption(Encryption encryption) {
        super(encryption.name());
    }

    public CannotUseEncryption(Encryption encryption, Encryption expected) {
        super("Got %s, instead of %s".formatted(encryption, expected));
    }
}
