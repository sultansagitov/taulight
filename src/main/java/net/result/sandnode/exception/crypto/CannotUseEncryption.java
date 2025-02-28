package net.result.sandnode.exception.crypto;

import net.result.sandnode.encryption.interfaces.Encryption;
import org.jetbrains.annotations.NotNull;

public class CannotUseEncryption extends CryptoException {
    public CannotUseEncryption(@NotNull Encryption encryption) {
        super(encryption);
    }

    public CannotUseEncryption(@NotNull Encryption encryption, Encryption expected) {
        super("Got %s, instead of %s".formatted(encryption.name(), expected));
    }
}
