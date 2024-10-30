package net.result.sandnode.util.encryption.symmetric.interfaces;

import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

public abstract class SymmetricKeyStorage implements IKeyStorage {
    protected SecretKey key;

    public @NotNull SecretKey getKey() {
        return this.key;
    }

    public @NotNull SymmetricKeyStorage setKey(@NotNull SecretKey key) {
        this.key = key;
        return this;
    }
}
