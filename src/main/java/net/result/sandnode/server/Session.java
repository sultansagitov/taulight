package net.result.sandnode.server;

import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);
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

    public void close() throws IOException {
        LOGGER.info("Closing connection with client");
        socket.close();
    }
}
