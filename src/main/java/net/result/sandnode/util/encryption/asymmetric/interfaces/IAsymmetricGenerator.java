package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.core.interfaces.IGenerator;
import org.jetbrains.annotations.NotNull;

public interface IAsymmetricGenerator extends IGenerator {

    @NotNull AsymmetricKeyStorage generateKeyStorage();

}