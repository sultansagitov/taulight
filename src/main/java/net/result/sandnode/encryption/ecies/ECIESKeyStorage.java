package net.result.sandnode.encryption.ecies;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.exceptions.CannotUseEncryption;
import org.jetbrains.annotations.NotNull;

import java.security.PrivateKey;
import java.security.PublicKey;

import static net.result.sandnode.encryption.AsymmetricEncryption.ECIES;

public final class ECIESKeyStorage implements IAsymmetricKeyStorage {
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
    public @NotNull IAsymmetricEncryption encryption() {
        return ECIES;
    }

    @Override
    public String encodedPublicKey() throws CannotUseEncryption {
        return ECIES.publicKeyConvertor().toEncodedString(this);
    }

    @Override
    public String encodedPrivateKey() throws CannotUseEncryption {
        return ECIES.privateKeyConvertor().toEncodedString(this);
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new ECIESKeyStorage(publicKey, privateKey);
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    public PrivateKey privateKey() {
        return privateKey;
    }
}
