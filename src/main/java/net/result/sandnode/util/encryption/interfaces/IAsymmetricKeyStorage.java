package net.result.sandnode.util.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IAsymmetricKeyStorage extends IKeyStorage {
    @Override
    @NotNull IAsymmetricEncryption encryption();
}