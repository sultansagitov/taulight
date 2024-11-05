package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;

public class NoSuchEncryptionException extends SandnodeException {

    public NoSuchEncryptionException(byte message) {
        super("No such encryption for " + message);
    }

}
