package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.EncryptionTypeException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface IAsymmetricKeyStorage extends IKeyStorage {
    @Override
    @NotNull IAsymmetricEncryption encryption();

    @CheckReturnValue
    @Contract(value = " -> this", pure = true)
    default IAsymmetricKeyStorage asymmetric() {
        return this;
    }

    @Contract(value = " -> fail")
    default ISymmetricKeyStorage symmetric() throws EncryptionTypeException {
        throw new EncryptionTypeException(encryption());
    }

    String encodedPublicKey() throws CannotUseEncryption;
    String encodedPrivateKey() throws CannotUseEncryption;
}