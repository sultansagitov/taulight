package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.*;
import org.jetbrains.annotations.NotNull;

public interface KeyStorage {
    @NotNull Encryption encryption();

    @NotNull KeyStorage copy();

    default KeyStorage expect(Encryption encryption) {
        if (encryption() == encryption) return this;
        throw new CannotUseEncryption(encryption(), encryption);
    }

    default AsymmetricKeyStorage asymmetric() {
        if (encryption().isAsymmetric()) return (AsymmetricKeyStorage) this;
        throw new EncryptionTypeException(encryption());
    }

    default SymmetricKeyStorage symmetric() {
        if (encryption().isSymmetric()) return (SymmetricKeyStorage) this;
        throw new EncryptionTypeException(encryption());
    }

    default String decrypt(byte[] data) {
        return encryption().decrypt(data, this);
    }

    default byte[] encrypt(String data) {
        return encryption().encrypt(data, this);
    }
}
