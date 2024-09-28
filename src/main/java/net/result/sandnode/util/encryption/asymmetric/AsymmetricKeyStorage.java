package net.result.sandnode.util.encryption.asymmetric;

import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AsymmetricKeyStorage implements IKeyStorage {
    protected PublicKey publicKey;
    protected PrivateKey privateKey;

    public AsymmetricKeyStorage setKeys(@NotNull KeyPair kp) {
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
        return this;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public AsymmetricKeyStorage setPublicKey(@NotNull PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public AsymmetricKeyStorage setPrivateKey(@NotNull PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }
}