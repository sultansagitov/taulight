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
    private final Map<IEncryption, IKeyStorage> keyStorageMap = new HashMap<>();

    public GlobalKeyStorage() {
    }

    public GlobalKeyStorage(@NotNull IKeyStorage keyStorage) {
        set(keyStorage);
    }

    public Optional<IKeyStorage> get(@NotNull IEncryption encryption) {
        IKeyStorage keyStorage = keyStorageMap.get(encryption);
        return keyStorage == null ? Optional.empty() : Optional.of(keyStorage);
    }

    public @NotNull IKeyStorage getNonNull(@NotNull IEncryption encryption) throws KeyStorageNotFoundException {
        if (has(encryption))
            return encryption == NONE ? NONE.generate() : keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public GlobalKeyStorage set(@NotNull IEncryption encryption, @NotNull IKeyStorage keyStorage) {
        if (keyStorage.encryption() == encryption && encryption != NONE)
            keyStorageMap.put(encryption, keyStorage);
        return this;
    }

    public GlobalKeyStorage set(@NotNull IKeyStorage keyStorage) {
        if (keyStorage.encryption() != NONE)
            keyStorageMap.put(keyStorage.encryption(), keyStorage);
        return this;
    }

    public boolean has(@NotNull IEncryption encryption) {
        return encryption == NONE || keyStorageMap.containsKey(encryption);
    }

    public Optional<IAsymmetricKeyStorage> getAsymmetric(@NotNull IAsymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<IKeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().asymmetric());
        return Optional.empty();
    }

    public @NotNull IAsymmetricKeyStorage getAsymmetricNonNull(@NotNull IAsymmetricEncryption encryption)
            throws KeyStorageNotFoundException, EncryptionTypeException {
        Optional<IAsymmetricKeyStorage> opt = getAsymmetric(encryption);
        if (opt.isPresent()) return opt.get();
        throw new KeyStorageNotFoundException(encryption);
    }

    public Optional<ISymmetricKeyStorage> getSymmetric(@NotNull ISymmetricEncryption encryption)
            throws EncryptionTypeException {
        Optional<IKeyStorage> opt = get(encryption);
        if (opt.isPresent()) return Optional.ofNullable(opt.get().symmetric());
        return Optional.empty();
    }

    public @NotNull ISymmetricKeyStorage getSymmetricNonNull(@NotNull ISymmetricEncryption encryption)
            throws CannotUseEncryption, EncryptionTypeException {
        Optional<ISymmetricKeyStorage> opt = getSymmetric(encryption);
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
        for (Map.Entry<IEncryption, IKeyStorage> entry : keyStorageMap.entrySet()) {
            IEncryption key = entry.getKey();
            IKeyStorage value = entry.getValue();
            builder.append(" ");
            builder.append(key.name());
            builder.append("=");
            builder.append(value.getClass().getSimpleName());
        }

        return builder.append(">").toString();
    }
}
