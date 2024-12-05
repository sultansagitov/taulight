package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IAsymmetricKeyStorage extends IKeyStorage {
    @Override
    @NotNull IAsymmetricEncryption encryption();
}