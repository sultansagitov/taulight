package net.result.sandnode.exception;

public class NoSuchEncryptionException extends EncryptionException {
    public NoSuchEncryptionException(byte encryptionByte) {
        super("No such encryption for %s".formatted(encryptionByte));
    }

    public NoSuchEncryptionException(String encryptionName) {
        super("No such encryption for %s".formatted(encryptionName));
    }
}
