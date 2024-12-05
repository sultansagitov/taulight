package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.WrongEncryptionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.result.sandnode.encryption.Encryption.NONE;

public class GlobalKeyStorage {
    private final Map<IEncryption, IKeyStorage> keyStorageMap = new HashMap<>();

    public GlobalKeyStorage() {
    }

    public GlobalKeyStorage(@NotNull IKeyStorage keyStorage) {
        set(keyStorage);
    }

    public @Nullable IKeyStorage get(@NotNull IEncryption encryption) {
        return keyStorageMap.get(encryption);
    }

    public @NotNull IKeyStorage getNonNull(@NotNull IEncryption encryption) throws KeyStorageNotFoundException {
        if (has(encryption))
            return encryption == NONE ? NONE.generator().generate() : keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public GlobalKeyStorage set(
            @NotNull IEncryption encryption,
            @NotNull IKeyStorage keyStorage
    ) {
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

    public IAsymmetricKeyStorage getAsymmetric(@NotNull IAsymmetricEncryption encryption)
            throws WrongEncryptionException {
        if (encryption.isAsymmetric())
            return (IAsymmetricKeyStorage) get(encryption);
        throw new WrongEncryptionException(encryption);
    }

    public ISymmetricKeyStorage getSymmetric(@NotNull ISymmetricEncryption encryption) throws WrongEncryptionException {
        if (encryption.isSymmetric())
            return (ISymmetricKeyStorage) get(encryption);
        throw new WrongEncryptionException(encryption);
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

