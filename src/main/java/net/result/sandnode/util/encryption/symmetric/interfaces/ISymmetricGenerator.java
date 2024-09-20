package net.result.sandnode.util.encryption.symmetric.interfaces;

import net.result.sandnode.util.encryption.interfaces.IGenerator;

public interface ISymmetricGenerator extends IGenerator {

    SymmetricKeyStorage generateKeyStorage();

}
