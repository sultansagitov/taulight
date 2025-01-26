package net.result.sandnode.encryption.none;

import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.encryption.Encryption.NONE;

public class NoneKeyStorage implements KeyStorage {
    @Override
    public @NotNull IEncryption encryption() {
        return NONE;
    }

    @Override
    public @NotNull KeyStorage copy() {
        return new NoneKeyStorage();
    }
}
