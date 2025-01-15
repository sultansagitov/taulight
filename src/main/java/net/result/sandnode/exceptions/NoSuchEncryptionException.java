package net.result.sandnode.exceptions;

public class NoSuchEncryptionException extends SandnodeException {
    public NoSuchEncryptionException(byte encryptionByte) {
        super("No such encryption for " + encryptionByte);
    }

    public NoSuchEncryptionException(String encryptionName) {
        super("No such encryption for %s".formatted(encryptionName));
    }
}
