package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IGenerator;

public interface IAsymmetricGenerator extends IGenerator {

    AsymmetricKeyStorage generateKeyStorage();

}