package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface AsymmetricConvertor {
    @NotNull AsymmetricKeyStorage toKeyStorage(@NotNull String encodedString);

    @NotNull String toEncodedString(@NotNull KeyStorage keyStorage);
}
