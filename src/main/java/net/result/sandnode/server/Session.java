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
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static net.result.sandnode.util.encryption.Encryption.NONE;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);
    public final GlobalKeyStorage sessionKeyStorage;
    public final Socket socket;
    protected final OutputStream out;
    protected Encryption encryption = NONE;
    private volatile boolean inBusy = false;
    private volatile boolean outBusy = false;

    public Session(
            @NotNull Socket socket,
            @NotNull GlobalKeyStorage serverKeyStorage
    ) throws IOException {
        this.socket = socket;
        out = socket.getOutputStream();
        sessionKeyStorage = serverKeyStorage.copy();
    }

    public static @NotNull RawMessage _receiveMessage(
            @NotNull InputStream in,
            @NotNull GlobalKeyStorage sessionKeyStorage
    ) throws NoSuchEncryptionException, ReadingKeyException, DecryptionException, NoSuchReqHandler, IOException {
        RawMessage request = Message.fromInput(in, sessionKeyStorage);
        LOGGER.info("Requested {}", request);
        return request;
    }

    public void setKey(@NotNull SymmetricKeyStorage symmetricKey) {
        this.encryption = symmetricKey.encryption();
        sessionKeyStorage.set(symmetricKey);
    }

    public void sendMessage(@NotNull IMessage response) throws IOException, ReadingKeyException, EncryptionException {
        sendMessage(response, encryption);
    }

    public void sendMessage(@NotNull IMessage response, @NotNull Encryption encryption) throws IOException,
            ReadingKeyException, EncryptionException {
        while (outBusy) {
            Thread.onSpinWait();
        }

        outBusy = true;
        out.write(response.toByteArray(sessionKeyStorage, encryption));
        outBusy = false;
    }

    public @NotNull RawMessage receiveMessage() throws IOException, NoSuchEncryptionException, ReadingKeyException,
            DecryptionException, NoSuchReqHandler {
        InputStream in = socket.getInputStream();

        while (inBusy) {
            Thread.onSpinWait();
        }

        inBusy = true;
        RawMessage rawMessage = _receiveMessage(in, sessionKeyStorage);
        inBusy = false;

        return rawMessage;
    }

    public void close() throws IOException {
        LOGGER.info("Closing connection with client");
        socket.close();
    }

    public @NotNull String getIPString() {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }
}
