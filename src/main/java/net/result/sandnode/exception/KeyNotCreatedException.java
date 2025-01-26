package net.result.sandnode.exception;

import net.result.sandnode.encryption.interfaces.Encryption;

public class KeyNotCreatedException extends EncryptionException {
    public KeyNotCreatedException(Encryption encryption) {
        super(encryption.name());
    }
}
