package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

public interface IKeyStorage {
    Encryption encryption();

    @NotNull IKeyStorage copy();
}
