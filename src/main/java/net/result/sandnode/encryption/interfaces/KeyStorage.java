package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.EncryptionException;
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

    default String decrypt(byte[] data)
            throws WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException, DecryptionException {
        return encryption().decrypt(data, this);
    }

    default byte[] encrypt(String data) throws EncryptionException, CryptoException {
        return encryption().encrypt(data, this);
    }
}
