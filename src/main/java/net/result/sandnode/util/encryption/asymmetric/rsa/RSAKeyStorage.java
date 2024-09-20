package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAKeyStorage extends AsymmetricKeyStorage {
    public RSAKeyStorage() {
    }

    public RSAKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.setPublicKey(globalKeyStorage.getRSAKeyStorage().getPublicKey());
        this.setPrivateKey(globalKeyStorage.getRSAKeyStorage().getPrivateKey());
    }

    public RSAKeyStorage setKeys(@NotNull KeyPair kp) {
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
        return this;
    }

    public RSAKeyStorage setPublicKey(@NotNull PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public RSAKeyStorage setPrivateKey(@NotNull PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }
}
