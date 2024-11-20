package net.result.sandnode.server;

import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.UnexpectedSocketDisconnect;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricKeyStorage;
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
    protected IEncryption encryption = NONE;
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

    public void setKey(@NotNull ISymmetricKeyStorage symmetricKey) {
        this.encryption = symmetricKey.encryption();
        sessionKeyStorage.set(symmetricKey);
    }

    public void sendMessage(@NotNull IMessage response) throws IOException, ReadingKeyException, EncryptionException,
            KeyStorageNotFoundException {
        sendMessage(response, encryption);
    }

    public void sendMessage(@NotNull IMessage response, @NotNull IEncryption encryption) throws IOException,
            ReadingKeyException, EncryptionException, KeyStorageNotFoundException {
        byte[] byteArray = response.toByteArray(sessionKeyStorage, encryption);
        if (outBusy) LOGGER.info("Waiting for sending message in other thread by {} {}", encryption.name(), response);
        while (outBusy) Thread.onSpinWait();
        outBusy = true;
        out.write(byteArray);
        outBusy = false;
        LOGGER.info("Sending message by {} {}", encryption.name(), response);
    }

    public @NotNull RawMessage receiveMessage() throws IOException, NoSuchEncryptionException, ReadingKeyException,
            DecryptionException, NoSuchReqHandler, UnexpectedSocketDisconnect, KeyStorageNotFoundException {
        InputStream in = socket.getInputStream();

        if (inBusy) LOGGER.info("Waiting for reading message in other thread");
        while (inBusy) Thread.onSpinWait();
        inBusy = true;
        Message.EncryptedMessage encrypted = Message.readMessage(in);
        inBusy = false;
        return Message.decryptMessage(sessionKeyStorage, encrypted);
    }

    public void close() throws IOException {
        LOGGER.info("Closing connection with client");
        socket.close();
    }

    public @NotNull String getIPString() {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }
}
