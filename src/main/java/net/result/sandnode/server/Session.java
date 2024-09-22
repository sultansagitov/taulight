package net.result.sandnode.server;

import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.UUID;

public class Session {
    public final Socket socket;
    public final UUID uuid;
    public SymmetricKeyStorage keyStorage;

    public Session(Socket socket) {
        this.socket = socket;
        this.uuid = UUID.randomUUID();
    }

    public void setKey(@NotNull SymmetricKeyStorage aesKey) {
        this.keyStorage = aesKey;
    }

}
