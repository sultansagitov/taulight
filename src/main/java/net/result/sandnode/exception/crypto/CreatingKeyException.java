package net.result.sandnode.exception.crypto;

import net.result.sandnode.encryption.interfaces.Encryption;
import org.jetbrains.annotations.NotNull;

public class CreatingKeyException extends CryptoException {
    public CreatingKeyException(@NotNull Encryption encryption, Throwable e) {
        super(encryption.name(), e);
    }
}
