package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class RSAKeyStorage extends AsymmetricKeyStorage {

    public RSAKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        PublicKey publicKey1 = globalKeyStorage.getRSAKeyStorage().getPublicKey();
        PrivateKey privateKey1 = globalKeyStorage.getRSAKeyStorage().getPrivateKey();
        if (publicKey1 != null) this.setPublicKey(publicKey1);
        if (privateKey1 != null) this.setPrivateKey(privateKey1);
    }

    public RSAKeyStorage(@NotNull KeyPair kp) {
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
    }

    @Override
    public Encryption encryption() {
        return RSA;
    }


    public RSAKeyStorage(@NotNull PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public RSAKeyStorage(@NotNull PrivateKey privateKey) {
        this.privateKey = privateKey;
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
