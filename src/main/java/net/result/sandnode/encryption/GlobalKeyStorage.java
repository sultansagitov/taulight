package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.EncryptionTypeException;
import net.result.sandnode.exception.KeyStorageNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.result.sandnode.encryption.Encryption.NONE;

public class GlobalKeyStorage {
    private final Map<IEncryption, KeyStorage> keyStorageMap = new HashMap<>();

    public GlobalKeyStorage() {
    }

    public GlobalKeyStorage(@NotNull KeyStorage keyStorage) {
        set(keyStorage);
    }

    public Optional<KeyStorage> get(@NotNull IEncryption encryption) {
        KeyStorage keyStorage = keyStorageMap.get(encryption);
        return keyStorage == null ? Optional.empty() : Optional.of(keyStorage);
    }

    public @NotNull KeyStorage getNonNull(@NotNull IEncryption encryption) throws KeyStorageNotFoundException {
        if (has(encryption))
            return encryption == NONE ? NONE.generate() : keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public GlobalKeyStorage set(@NotNull IEncryption encryption, @NotNull KeyStorage keyStorage) {
        if (keyStorage.encryption() == encryption && encryption != NONE)
            keyStorageMap.put(encryption, keyStorage);
        return this;
    }

    public GlobalKeyStorage set(@NotNull KeyStorage keyStorage) {
        if (keyStorage.encryption() != NONE)
            keyStorageMap.put(keyStorage.encryption(), keyStorage);
        return this;
    }

    public boolean has(@NotNull IEncryption encryption) {
        return encryption == NONE || keyStorageMap.containsKey(encryption);
    }

    public Optional<AsymmetricKeyStorage> getAsymmetric(@NotNull IAsymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<KeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().asymmetric());
        return Optional.empty();
    }

    public @NotNull AsymmetricKeyStorage getAsymmetricNonNull(@NotNull IAsymmetricEncryption encryption)
            throws KeyStorageNotFoundException, EncryptionTypeException {
        Optional<AsymmetricKeyStorage> opt = getAsymmetric(encryption);
        if (opt.isPresent()) return opt.get();
        throw new KeyStorageNotFoundException(encryption);
    }

    public Optional<SymmetricKeyStorage> getSymmetric(@NotNull ISymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<KeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().symmetric());
        return Optional.empty();
    }

    public @NotNull SymmetricKeyStorage getSymmetricNonNull(@NotNull ISymmetricEncryption encryption)
            throws CannotUseEncryption, EncryptionTypeException {
        Optional<SymmetricKeyStorage> opt = getSymmetric(encryption);
        if (opt.isPresent()) return opt.get();
        throw new CannotUseEncryption(encryption);
    }

    public GlobalKeyStorage copy() {
        GlobalKeyStorage copy = new GlobalKeyStorage();
        keyStorageMap.forEach((key, value) -> copy.set(key, value.copy()));
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(getClass().getSimpleName());
        for (Map.Entry<IEncryption, KeyStorage> entry : keyStorageMap.entrySet()) {
            IEncryption key = entry.getKey();
            KeyStorage value = entry.getValue();
            builder.append(" ");
            builder.append(key.name());
            builder.append("=");
            builder.append(value.getClass().getSimpleName());
        }

        return builder.append(">").toString();
    }
}
