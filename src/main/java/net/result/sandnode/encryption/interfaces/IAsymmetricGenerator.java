package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IAsymmetricGenerator extends IGenerator {

    @NotNull IAsymmetricKeyStorage generate();

}