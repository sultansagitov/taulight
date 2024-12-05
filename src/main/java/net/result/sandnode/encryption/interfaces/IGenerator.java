package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IGenerator {

    @NotNull IKeyStorage generate();

}
