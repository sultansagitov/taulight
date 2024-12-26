package net.result.sandnode.exceptions;

import net.result.sandnode.encryption.EncryptionManager;

public class NoSuchEncryptionException extends SandnodeException {

    public NoSuchEncryptionException(byte encryptionByte) {
        super("No such encryption for " + encryptionByte);
    }

    public NoSuchEncryptionException(String encryptionName) {
        super("No such encryption for " + encryptionName + ", registered: " + EncryptionManager.instance().list.size());
    }

}
