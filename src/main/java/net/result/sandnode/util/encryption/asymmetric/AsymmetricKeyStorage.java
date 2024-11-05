package net.result.sandnode.util.encryption.asymmetric;

import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AsymmetricKeyStorage implements IKeyStorage {
    private PublicKey publicKey;
    private PrivateKey privateKey;

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
}