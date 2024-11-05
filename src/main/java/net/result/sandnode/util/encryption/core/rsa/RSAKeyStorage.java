package net.result.sandnode.util.encryption.core.rsa;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class RSAKeyStorage extends AsymmetricKeyStorage {

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

    @Override
    public @NotNull Encryption encryption() {
        return RSA;
    }

    @Override
    public @NotNull IKeyStorage copy() {
        return new RSAKeyStorage(getPublicKey(), getPrivateKey());
    }

}
