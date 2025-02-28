package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KeyStorageRegistry {
    private final Map<Encryption, KeyStorage> keyStorageMap = new HashMap<>();

    public KeyStorageRegistry() {
    }

    public KeyStorageRegistry(@NotNull KeyStorage keyStorage) {
        set(keyStorage);
    }

    public Optional<KeyStorage> get(@NotNull Encryption encryption) {
        return Optional.ofNullable(keyStorageMap.get(encryption));
    }

    public @NotNull KeyStorage getNonNull(@NotNull Encryption encryption) throws KeyStorageNotFoundException {
        if (has(encryption))
            return encryption == Encryptions.NONE ? Encryptions.NONE.generate() : keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public KeyStorageRegistry set(@NotNull Encryption encryption, @NotNull KeyStorage keyStorage) {
        if (keyStorage.encryption() == encryption && encryption != Encryptions.NONE)
            keyStorageMap.put(encryption, keyStorage);
        return this;
    }

    public KeyStorageRegistry set(@NotNull KeyStorage keyStorage) {
        if (keyStorage.encryption() != Encryptions.NONE)
            keyStorageMap.put(keyStorage.encryption(), keyStorage);
        return this;
    }

    public boolean has(@NotNull Encryption encryption) {
        return encryption == Encryptions.NONE || keyStorageMap.containsKey(encryption);
    }

    public Optional<AsymmetricKeyStorage> asymmetric(@NotNull AsymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<KeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().asymmetric());
        return Optional.empty();
    }

    public @NotNull AsymmetricKeyStorage asymmetricNonNull(@NotNull AsymmetricEncryption encryption)
            throws KeyStorageNotFoundException, EncryptionTypeException {
        Optional<AsymmetricKeyStorage> opt = asymmetric(encryption);
        if (opt.isPresent()) return opt.get();
        throw new KeyStorageNotFoundException(encryption);
    }

    public Optional<SymmetricKeyStorage> symmetric(@NotNull SymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<KeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().symmetric());
        return Optional.empty();
    }

    public @NotNull SymmetricKeyStorage symmetricNonNull(@NotNull SymmetricEncryption encryption)
            throws CannotUseEncryption, EncryptionTypeException {
        Optional<SymmetricKeyStorage> opt = symmetric(encryption);
        if (opt.isPresent()) return opt.get();
        throw new CannotUseEncryption(encryption);
    }

    public KeyStorageRegistry copy() {
        KeyStorageRegistry copy = new KeyStorageRegistry();
        keyStorageMap.forEach((key, value) -> copy.set(key, value.copy()));
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(getClass().getSimpleName());
        for (Map.Entry<Encryption, KeyStorage> entry : keyStorageMap.entrySet()) {
            Encryption key = entry.getKey();
            KeyStorage value = entry.getValue();
            builder.append(" ");
            builder.append(key.name());
            builder.append("=");
            builder.append(value.getClass().getSimpleName());
        }

        return builder.append(">").toString();
    }
}
