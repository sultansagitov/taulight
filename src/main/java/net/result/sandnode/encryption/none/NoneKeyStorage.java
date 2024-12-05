package net.result.sandnode.encryption.none;

import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.encryption.Encryption.NONE;

public class NoneKeyStorage implements IKeyStorage {
    @Override
    public @NotNull IEncryption encryption() {
        return NONE;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new NoneKeyStorage();
    }
}
