package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface AsymmetricKeyStorage extends KeyStorage {
    @Override
    @NotNull AsymmetricEncryption encryption();

    @Contract(value = " -> this", pure = true)
    default AsymmetricKeyStorage asymmetric() {
        return this;
    }

    @Contract(value = " -> fail")
    default SymmetricKeyStorage symmetric() throws EncryptionTypeException {
        throw new EncryptionTypeException(encryption());
    }

    String encodedPublicKey() throws CannotUseEncryption;
    String encodedPrivateKey() throws CannotUseEncryption;
}