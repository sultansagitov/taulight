package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IKeyStorage {
    @NotNull IEncryption encryption();

    @NotNull IKeyStorage copy();
}
