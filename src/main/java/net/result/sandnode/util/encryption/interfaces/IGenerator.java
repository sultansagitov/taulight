package net.result.sandnode.util.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IGenerator {

    @NotNull IKeyStorage generate();

}
