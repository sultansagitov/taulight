package net.result.sandnode.util.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface IAsymmetricGenerator extends IGenerator {

    @NotNull IAsymmetricKeyStorage generate();

}