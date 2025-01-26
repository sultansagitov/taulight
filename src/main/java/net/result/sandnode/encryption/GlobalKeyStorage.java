package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.EncryptionTypeException;
import net.result.sandnode.exception.KeyStorageNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.result.sandnode.encryption.Encryptions.NONE;

public class GlobalKeyStorage {
    private final Map<Encryption, KeyStorage> keyStorageMap = new HashMap<>();

    public GlobalKeyStorage() {
    }

    public GlobalKeyStorage(@NotNull KeyStorage keyStorage) {
        set(keyStorage);
    }

    public Optional<KeyStorage> get(@NotNull Encryption encryption) {
        return Optional.ofNullable(keyStorageMap.get(encryption));
    }

    public @NotNull KeyStorage getNonNull(@NotNull Encryption encryption) throws KeyStorageNotFoundException {
        if (has(encryption))
            return encryption == NONE ? NONE.generate() : keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public GlobalKeyStorage set(@NotNull Encryption encryption, @NotNull KeyStorage keyStorage) {
        if (keyStorage.encryption() == encryption && encryption != NONE)
            keyStorageMap.put(encryption, keyStorage);
        return this;
    }

    public GlobalKeyStorage set(@NotNull KeyStorage keyStorage) {
        if (keyStorage.encryption() != NONE)
            keyStorageMap.put(keyStorage.encryption(), keyStorage);
        return this;
    }

    public boolean has(@NotNull Encryption encryption) {
        return encryption == NONE || keyStorageMap.containsKey(encryption);
    }

    public Optional<AsymmetricKeyStorage> getAsymmetric(@NotNull AsymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<KeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().asymmetric());
        return Optional.empty();
    }

    public @NotNull AsymmetricKeyStorage getAsymmetricNonNull(@NotNull AsymmetricEncryption encryption)
            throws KeyStorageNotFoundException, EncryptionTypeException {
        Optional<AsymmetricKeyStorage> opt = getAsymmetric(encryption);
        if (opt.isPresent()) return opt.get();
        throw new KeyStorageNotFoundException(encryption);
    }

    public Optional<SymmetricKeyStorage> getSymmetric(@NotNull SymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<KeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().symmetric());
        return Optional.empty();
    }

    public @NotNull SymmetricKeyStorage getSymmetricNonNull(@NotNull SymmetricEncryption encryption)
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
