package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.EncryptionTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ISymmetricKeyStorage extends IKeyStorage {
    @Override
    @NotNull ISymmetricEncryption encryption();

    byte[] toBytes();

    @Contract(value = " -> fail")
    default IAsymmetricKeyStorage asymmetric() throws EncryptionTypeException {
        throw new EncryptionTypeException(encryption());
    }

    @Contract(value = " -> this")
    default ISymmetricKeyStorage symmetric() {
        return this;
    }
}
