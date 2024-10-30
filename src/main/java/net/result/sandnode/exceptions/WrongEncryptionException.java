package net.result.sandnode.exceptions;

import net.result.sandnode.util.encryption.Encryption;

public class WrongEncryptionException extends RuntimeException {
    public WrongEncryptionException(Encryption encryption) {
        super(encryption.name());
    }
}
