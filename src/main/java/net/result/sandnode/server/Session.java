package net.result.sandnode.server;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.EncryptionFactory;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static net.result.sandnode.util.encryption.Encryption.NO;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);

    private final OutputStream out;
    public final GlobalKeyStorage sessionKeyStorage;

    public final Socket socket;
    public final UUID uuid = UUID.randomUUID();
    private Encryption encryption = NO;

    public Session(@NotNull Socket socket, @NotNull GlobalKeyStorage serverKeyStorage) throws IOException {
        this.socket = socket;
        out = socket.getOutputStream();
        sessionKeyStorage = serverKeyStorage.copy();
    }

    public void setKey(@NotNull Encryption encryption, @NotNull SymmetricKeyStorage symmetricKey) {
        this.encryption = encryption;
        EncryptionFactory.setKeyStorage(sessionKeyStorage, this.encryption, symmetricKey);
    }

    public void sendMessage(
            @NotNull IMessage response
    ) throws IOException, ReadingKeyException, EncryptionException {
        out.write(response.toByteArray(this.sessionKeyStorage, encryption));
    }

    public void close() throws IOException {
        LOGGER.info("Closing connection with client");
        socket.close();
    }

    public @NotNull RawMessage receiveMessage() throws
            IOException, NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException,
            NoSuchReqHandler {
        return Message.fromInput(socket.getInputStream(), sessionKeyStorage);
    }
}
