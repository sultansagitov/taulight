package net.result.sandnode.util.encryption.symmetric.interfaces;

import net.result.sandnode.util.encryption.core.interfaces.IGenerator;
import org.jetbrains.annotations.NotNull;

public interface ISymmetricGenerator extends IGenerator {

    @NotNull SymmetricKeyStorage generateKeyStorage();

}
