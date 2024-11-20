package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import net.result.sandnode.util.encryption.Encryptions;

public class NoSuchEncryptionException extends SandnodeException {

    public NoSuchEncryptionException(byte encryptionByte) {
        super("No such encryption for " + encryptionByte);
    }

    public NoSuchEncryptionException(String encryptionName) {
        super("No such encryption for " + encryptionName + ", registered: " + Encryptions.list.size());
    }

}
