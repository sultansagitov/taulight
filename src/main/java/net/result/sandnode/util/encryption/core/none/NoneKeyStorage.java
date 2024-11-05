package net.result.sandnode.util.encryption.core.none;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.util.encryption.Encryption.NONE;

public class NoneKeyStorage implements IKeyStorage {
    @Override
    public @NotNull Encryption encryption() {
        return NONE;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new NoneKeyStorage();
    }
}
