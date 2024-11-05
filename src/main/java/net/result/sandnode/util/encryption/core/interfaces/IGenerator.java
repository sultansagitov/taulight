package net.result.sandnode.util.encryption.core.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IGenerator {

    @NotNull IKeyStorage generateKeyStorage();

}
