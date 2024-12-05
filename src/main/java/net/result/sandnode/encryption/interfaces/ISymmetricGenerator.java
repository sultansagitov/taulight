package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface ISymmetricGenerator extends IGenerator {

    @NotNull ISymmetricKeyStorage generate();

}
