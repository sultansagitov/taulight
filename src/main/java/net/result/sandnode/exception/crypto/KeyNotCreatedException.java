package net.result.sandnode.exception.crypto;

import net.result.sandnode.encryption.interfaces.Encryption;
import org.jetbrains.annotations.NotNull;

public class KeyNotCreatedException extends CryptoException {
    public KeyNotCreatedException(@NotNull Encryption encryption) {
        super(encryption);
    }
}
