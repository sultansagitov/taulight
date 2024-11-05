package net.result.sandnode.util.encryption.core.interfaces;

import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

public interface IKeyStorage {
    @NotNull Encryption encryption();

    @NotNull IKeyStorage copy();
}
