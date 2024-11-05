package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.WrongEncryptionException;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static net.result.sandnode.util.encryption.Encryption.NONE;

public class GlobalKeyStorage {
    private final Map<Encryption, IKeyStorage> keyStorageMap = new EnumMap<>(Encryption.class);

    public GlobalKeyStorage() {
    }

    public GlobalKeyStorage(@NotNull IKeyStorage keyStorage) {
        set(keyStorage);
    }

    public @Nullable IKeyStorage get(@NotNull Encryption encryption) {
        return keyStorageMap.get(encryption);
    }

    public @NotNull IKeyStorage getNonNull(@NotNull Encryption encryption) {
        if (has(encryption))
            return encryption == NONE ? NONE.generator().generateKeyStorage() : keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public GlobalKeyStorage set(
            @NotNull Encryption encryption,
            @NotNull IKeyStorage keyStorage
    ) {
        if (keyStorage.encryption() == encryption) {
            keyStorageMap.put(encryption, keyStorage);
        }
        return this;
    }

    public GlobalKeyStorage set(@NotNull IKeyStorage keyStorage) {
        keyStorageMap.put(keyStorage.encryption(), keyStorage);
        return this;
    }

    public boolean has(@NotNull Encryption encryption) {
        return encryption == NONE || keyStorageMap.containsKey(encryption);
    }

    public AsymmetricKeyStorage getAsymmetric(@NotNull Encryption encryption) {
        if (encryption.isAsymmetric)
            return (AsymmetricKeyStorage) get(encryption);
        throw new WrongEncryptionException(encryption);
    }

    public SymmetricKeyStorage getSymmetric(@NotNull Encryption encryption) {
        if (encryption.isSymmetric)
            return (SymmetricKeyStorage) get(encryption);
        throw new WrongEncryptionException(encryption);
    }

    public GlobalKeyStorage copy() {
        GlobalKeyStorage copy = new GlobalKeyStorage();
        for (Encryption value : Encryption.values()) {
            if (has(value)) copy.set(value, getNonNull(value).copy());
        }
        return copy;
    }
}
