package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.EncryptionTypeException;
import org.jetbrains.annotations.NotNull;

public interface IKeyStorage {
    @NotNull IEncryption encryption();

    @NotNull IKeyStorage copy();

    default IKeyStorage expect(IEncryption encryption) throws CannotUseEncryption {
        if (encryption() == encryption) return this;
        throw new CannotUseEncryption(encryption(), encryption);
    }

    default IAsymmetricKeyStorage asymmetric() throws EncryptionTypeException {
        if (encryption().isAsymmetric()) return (IAsymmetricKeyStorage) this;
        throw new EncryptionTypeException(encryption());
    }

    default ISymmetricKeyStorage symmetric() throws EncryptionTypeException {
        if (encryption().isSymmetric()) return (ISymmetricKeyStorage) this;
        throw new EncryptionTypeException(encryption());
    }
}
