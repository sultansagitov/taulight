package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import org.jetbrains.annotations.NotNull;

public interface KeyStorage {
    @NotNull Encryption encryption();

    @NotNull KeyStorage copy();

    default KeyStorage expect(Encryption encryption) throws CannotUseEncryption {
        if (encryption() == encryption) return this;
        throw new CannotUseEncryption(encryption(), encryption);
    }

    default AsymmetricKeyStorage asymmetric() throws EncryptionTypeException {
        if (encryption().isAsymmetric()) return (AsymmetricKeyStorage) this;
        throw new EncryptionTypeException(encryption());
    }

    default SymmetricKeyStorage symmetric() throws EncryptionTypeException {
        if (encryption().isSymmetric()) return (SymmetricKeyStorage) this;
        throw new EncryptionTypeException(encryption());
    }
}
