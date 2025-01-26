package net.result.sandnode.encryption.none;

import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.encryption.Encryptions.NONE;

public class NoneKeyStorage implements KeyStorage {
    @Override
    public @NotNull Encryption encryption() {
        return NONE;
    }

    @Override
    public @NotNull KeyStorage copy() {
        return new NoneKeyStorage();
    }
}
