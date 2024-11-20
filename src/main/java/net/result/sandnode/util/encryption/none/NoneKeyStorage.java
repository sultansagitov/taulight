package net.result.sandnode.util.encryption.none;

import net.result.sandnode.util.encryption.interfaces.IEncryption;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.util.encryption.Encryption.NONE;

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
