package net.result.sandnode.encryption.interfaces;

public interface IAsymmetricEncryption extends IEncryption {
    IAsymmetricConvertor publicKeyConvertor();

    IAsymmetricConvertor privateKeyConvertor();

    IAsymmetricKeySaver keySaver();

    IAsymmetricKeyReader keyReader();
}
