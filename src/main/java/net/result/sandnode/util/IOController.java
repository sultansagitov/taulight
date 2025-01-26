package net.result.sandnode.util;

import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.ExitMessage;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.result.sandnode.encryption.Encryption.NONE;

public class IOController {
    private static final Logger LOGGER = LogManager.getLogger(IOController.class);

    private final Connection connection;
    public final GlobalKeyStorage globalKeyStorage;

    private final InputStream in;
    private final OutputStream out;
    public final Socket socket;
    private final BlockingQueue<IMessage> sendingQueue = new LinkedBlockingQueue<>();
    public final ChainManager chainManager;

    private IEncryption serverEncryption = NONE;
    private IEncryption symKeyEncryption = NONE;
    public boolean connected = true;

    public IOController(
            Socket socket,
            Connection connection,
            GlobalKeyStorage globalKeyStorage,
            ChainManager chainManager
    ) throws InputStreamException, OutputStreamException {
        this.in = StreamReader.inputStream(socket);
        this.out = StreamReader.outputStream(socket);
        this.socket = socket;
        this.connection = connection;
        this.globalKeyStorage = globalKeyStorage.copy();
        this.chainManager = chainManager;
    }

    private void beforeSending(IMessage message) {
        message.getHeaders().setConnection(connection);
        if (message.getHeadersEncryption() == NONE) {
            message.setHeadersEncryption(getCurrentEncryption());
        }
        if (message.getHeaders().getBodyEncryption() == NONE) {
            message.getHeaders().setBodyEncryption(getCurrentEncryption());
        }
        Random random = new SecureRandom();
        String s = "0123456789abcdefghijklmnopqrstuvwxyz!@$%&*()_+-={}[]\"'<>?,./ ~";
        String sb = IntStream
                .range(0, random.nextInt(16, 32))
                .mapToObj(i -> "" + s.charAt(random.nextInt(61)))
                .collect(Collectors.joining());
        message.getHeaders().setValue("random", sb);
    }

    public void sendingLoop() throws InterruptedException, IllegalMessageLengthException,
            MessageSerializationException, EncryptionException, MessageWriteException {
        while (connected) {
            IMessage message = sendingQueue.take();
            beforeSending(message);

            IMessage sent = null;
            byte[] byteArray = null;
            SandnodeError error = null;
            ErrorMessage errorMessage;
            try {
                byteArray = message.toByteArray(globalKeyStorage);
                sent = message;
            } catch (MessageSerializationException | IllegalMessageLengthException e) {
                LOGGER.error("Serialization or message length issue", e);
                error = Errors.SERVER_ERROR;
            } catch (KeyStorageNotFoundException e) {
                LOGGER.error("Key storage not found", e);
                error = Errors.KEY_NOT_FOUND;
            } catch (EncryptionException e) {
                LOGGER.error("Encryption or key issue", e);
                error = Errors.ENCRYPT;
            }


            if (error != null) {
                errorMessage = error.message();
                Headers headers = errorMessage.getHeaders();
                errorMessage
                        .setHeadersEncryption(message.getHeadersEncryption());
                headers
                        .setBodyEncryption(message.getHeaders().getBodyEncryption())
                        .setChainID(message.getHeaders().getChainID())
                        .setConnection(message.getHeaders().getConnection());
                byteArray = errorMessage.toByteArray(globalKeyStorage);
                sent = errorMessage;
            }

            try {
                out.write(byteArray);
                out.flush();
            } catch (IOException e) {
                throw new MessageWriteException(message, "Failed to write message to output.", e);
            }

            LOGGER.info("Message sent: {}", sent);
        }
    }

    public void receivingLoop() throws UnexpectedSocketDisconnectException, DecryptionException,
            NoSuchMessageTypeException, NoSuchEncryptionException, KeyStorageNotFoundException,
            WrongKeyException, InterruptedException, PrivateKeyNotFoundException {
        while (connected) {
            EncryptedMessage encrypted = EncryptedMessage.readMessage(in);
            RawMessage message = Message.decryptMessage(encrypted, globalKeyStorage);
            chainManager.distributeMessage(message);
        }
    }

    public static @NotNull String getIpString(@NotNull Socket socket) {
        return "%s:%d".formatted(socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    public @NotNull String getIpString() {
        return getIpString(socket);
    }

    public @NotNull IEncryption getServerEncryption() {
        return serverEncryption;
    }

    public @NotNull IEncryption getSymKeyEncryption() {
        return symKeyEncryption;
    }

    public synchronized boolean isConnected() {
        if (!socket.isConnected()) {
            return false;
        }

        synchronized (this) {
            return connected;
        }
    }

    public void setServerKey(@NotNull AsymmetricKeyStorage publicKey) {
        serverEncryption = publicKey.encryption();
        globalKeyStorage.set(publicKey);
    }

    public void setClientKey(@NotNull SymmetricKeyStorage symmetricKeyStorage) {
        symKeyEncryption = symmetricKeyStorage.encryption();
        globalKeyStorage.set(symmetricKeyStorage);
    }

    private @NotNull IEncryption getCurrentEncryption() {
        return getSymKeyEncryption() != NONE ? getSymKeyEncryption() : getServerEncryption();
    }

    public void sendMessage(@NotNull IMessage message) throws InterruptedException {
        if (message.getHeadersEncryption() == NONE)
            message.setHeadersEncryption(getCurrentEncryption());

        sendingQueue.put(message);
    }

    public synchronized void disconnect() throws SocketClosingException, InterruptedException {

        synchronized (this) {
            connected = false;
        }

        LOGGER.info("Sending exit message");
        sendMessage(new ExitMessage());
        LOGGER.info("Disconnecting from {}", getIpString(socket));

        chainManager.interruptAll();

        try {
            socket.close();
        } catch (IOException e) {
            throw new SocketClosingException("Cannot close socket", e);
        }
    }
}
