package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.EncryptionTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface SymmetricKeyStorage extends KeyStorage {
    @Override
    @NotNull ISymmetricEncryption encryption();

    byte[] toBytes();

    @Contract(value = " -> fail")
    default AsymmetricKeyStorage asymmetric() throws EncryptionTypeException {
        throw new EncryptionTypeException(encryption());
    }

    @Contract(value = " -> this")
    default SymmetricKeyStorage symmetric() {
        return this;
    }
}
