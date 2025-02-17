package net.result.sandnode.encryption.ecies;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import org.jetbrains.annotations.NotNull;

import java.security.PrivateKey;
import java.security.PublicKey;

public final class ECIESKeyStorage implements AsymmetricKeyStorage {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public ECIESKeyStorage(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public ECIESKeyStorage(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public ECIESKeyStorage(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public @NotNull AsymmetricEncryption encryption() {
        return AsymmetricEncryptions.ECIES;
    }

    @Override
    public String encodedPublicKey() throws CannotUseEncryption {
        return AsymmetricEncryptions.ECIES.publicKeyConvertor().toEncodedString(this);
    }

    @Override
    public String encodedPrivateKey() throws CannotUseEncryption {
        return AsymmetricEncryptions.ECIES.privateKeyConvertor().toEncodedString(this);
    }

    @Override
    public @NotNull KeyStorage copy() {
        return new ECIESKeyStorage(publicKey, privateKey);
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    public PrivateKey privateKey() {
        return privateKey;
    }
}
