package net.result.sandnode.util.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IKeyStorage {
    @NotNull IEncryption encryption();

    @NotNull IKeyStorage copy();
}
