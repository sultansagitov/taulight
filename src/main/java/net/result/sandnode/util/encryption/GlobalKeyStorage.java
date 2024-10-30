package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.WrongEncryptionException;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class GlobalKeyStorage {

    private final Map<Encryption, IKeyStorage> keyStorageMap = new EnumMap<>(Encryption.class);

    public @Nullable IKeyStorage get(@NotNull Encryption encryption) {
        return keyStorageMap.get(encryption);
    }

    public @NotNull IKeyStorage getNonNull(@NotNull Encryption encryption) {
        if (has(encryption))
            return keyStorageMap.get(encryption);
        throw new KeyStorageNotFoundException(encryption);
    }

    public void set(
            @NotNull Encryption encryption,
            @NotNull IKeyStorage keyStorage
    ) {
        if (keyStorage.encryption() == encryption) {
            keyStorageMap.put(encryption, keyStorage);
        }
    }

    public boolean has(@NotNull Encryption encryption) {
        return keyStorageMap.containsKey(encryption);
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
