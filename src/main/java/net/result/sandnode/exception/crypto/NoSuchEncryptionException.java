package net.result.sandnode.exception.crypto;

public class NoSuchEncryptionException extends CryptoException {
    public NoSuchEncryptionException(byte encryptionByte) {
        super("No such encryption for %s".formatted(encryptionByte));
    }

    public NoSuchEncryptionException(String encryptionName) {
        super("No such encryption for %s".formatted(encryptionName));
    }
}
