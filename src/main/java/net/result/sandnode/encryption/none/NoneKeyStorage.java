package net.result.sandnode.encryption.none;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.jetbrains.annotations.NotNull;

public class NoneKeyStorage implements KeyStorage {
    @Override
    public @NotNull Encryption encryption() {
        return Encryptions.NONE;
    }

    @Override
    public @NotNull KeyStorage copy() {
        return new NoneKeyStorage();
    }
}
