package net.result.sandnode.server;

import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.util.UUID;

public class Session {
    public UUID uuid;
    public SymmetricKeyStorage keyStorage;
    private String userAgent;

    public Session() {
        this.uuid = UUID.randomUUID();
    }

    public void setKey(@NotNull SymmetricKeyStorage aesKey) {
        this.keyStorage = aesKey;
    }

    public void setKey(@NotNull SecretKey aesKey) {
        this.keyStorage = new AESKeyStorage().setKey(aesKey);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
