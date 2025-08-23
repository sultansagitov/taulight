package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.EncryptionTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

public interface SymmetricKeyStorage extends KeyStorage {
    @Override
    @NotNull SymmetricEncryption encryption();

    byte[] toBytes();

    @Contract(value = " -> fail")
    default AsymmetricKeyStorage asymmetric() {
        throw new EncryptionTypeException(encryption());
    }

    @Contract(value = " -> this")
    default SymmetricKeyStorage symmetric() {
        return this;
    }

    default String encoded() {
        return Base64.getEncoder().encodeToString(toBytes());
    }
}
