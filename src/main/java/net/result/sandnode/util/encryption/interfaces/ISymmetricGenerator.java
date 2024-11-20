package net.result.sandnode.util.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface ISymmetricGenerator extends IGenerator {

    @NotNull ISymmetricKeyStorage generate();

}
