package net.result.sandnode.encryption.rsa;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;

public class RSAKeyStorage implements IAsymmetricKeyStorage {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RSAKeyStorage(@NotNull KeyPair kp) {
        this.setKeys(kp);
    }

    public RSAKeyStorage(@NotNull PublicKey publicKey) {
        this.set(publicKey);
    }

    public RSAKeyStorage(@NotNull PrivateKey privateKey) {
        this.set(privateKey);
    }

    public RSAKeyStorage(@NotNull PublicKey publicKey, @NotNull PrivateKey privateKey) {
        this.set(publicKey);
        this.set(privateKey);
    }

    public void setKeys(@NotNull KeyPair kp) {
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public void set(@NotNull PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void set(@NotNull PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public @NotNull IAsymmetricEncryption encryption() {
        return RSA;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new RSAKeyStorage(getPublicKey(), getPrivateKey());
    }

}
