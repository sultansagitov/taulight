package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface ISymmetricKeyStorage extends IKeyStorage {
    @Override
    @NotNull ISymmetricEncryption encryption();
}
