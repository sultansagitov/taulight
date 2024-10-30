package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class RSAKeyStorage extends AsymmetricKeyStorage {

    public RSAKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        AsymmetricKeyStorage asymmetricKeyStorage = globalKeyStorage.getAsymmetric(RSA);
        PublicKey publicKey1 = asymmetricKeyStorage.getPublicKey();
        PrivateKey privateKey1 = asymmetricKeyStorage.getPrivateKey();
        if (publicKey1 != null) this.setPublicKey(publicKey1);
        if (privateKey1 != null) this.setPrivateKey(privateKey1);
    }

    public RSAKeyStorage(@NotNull KeyPair kp) {
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
    }

    public RSAKeyStorage(@NotNull PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public RSAKeyStorage(@NotNull PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public RSAKeyStorage(@NotNull PublicKey publicKey, @NotNull PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public Encryption encryption() {
        return RSA;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new RSAKeyStorage(publicKey, privateKey);
    }

    @Override
    public RSAKeyStorage setKeys(@NotNull KeyPair kp) {
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
        return this;
    }

    @Override
    public RSAKeyStorage setPublicKey(@NotNull PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    @Override
    public RSAKeyStorage setPrivateKey(@NotNull PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }
}
