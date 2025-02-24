package net.result.sandnode.util;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.KeyStorageRegistry;
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

public class IOController {
    private static final Logger LOGGER = LogManager.getLogger(IOController.class);

    private final Connection connection;
    public final KeyStorageRegistry keyStorageRegistry;

    private final InputStream in;
    private final OutputStream out;
    public final Socket socket;
    private final BlockingQueue<IMessage> sendingQueue = new LinkedBlockingQueue<>();
    public final ChainManager chainManager;

    private Encryption serverEncryption = Encryptions.NONE;
    private Encryption symKeyEncryption = Encryptions.NONE;
    public boolean connected = true;

    public IOController(
            Socket socket,
            Connection connection,
            KeyStorageRegistry keyStorageRegistry,
            ChainManager chainManager
    ) throws InputStreamException, OutputStreamException {
        this.in = StreamReader.inputStream(socket);
        this.out = StreamReader.outputStream(socket);
        this.socket = socket;
        this.connection = connection;
        this.keyStorageRegistry = keyStorageRegistry.copy();
        this.chainManager = chainManager;
    }

    private void beforeSending(IMessage message) {
        Headers headers = message.headers();
        headers.setConnection(connection);
        if (message.headersEncryption() == Encryptions.NONE) {
            message.setHeadersEncryption(currentEncryption());
        }
        if (headers.bodyEncryption() == Encryptions.NONE) {
            headers.setBodyEncryption(currentEncryption());
        }
        Random random = new SecureRandom();
        String s = "0123456789abcdefghijklmnopqrstuvwxyz!@$%&*()_+-={}[]\"'<>?,./ ~";
        String sb = IntStream
                .range(0, random.nextInt(16, 32))
                .mapToObj(i -> "" + s.charAt(random.nextInt(61)))
                .collect(Collectors.joining());
        headers.setValue("random", sb);
    }

    public void sendingLoop() throws InterruptedException, IllegalMessageLengthException,
            MessageSerializationException, EncryptionException, MessageWriteException {
        while (connected) {
            IMessage message = sendingQueue.take();
            beforeSending(message);

            IMessage sent = null;
            byte[] byteArray = null;
            SandnodeError error = null;
            try {
                byteArray = message.toByteArray(keyStorageRegistry);
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
                ErrorMessage errorMessage = error.createMessage();
                Headers headers = errorMessage.headers();
                errorMessage
                        .setHeadersEncryption(message.headersEncryption());
                headers
                        .setBodyEncryption(message.headers().bodyEncryption())
                        .setChainID(message.headers().chainID())
                        .setConnection(message.headers().connection());
                byteArray = errorMessage.toByteArray(keyStorageRegistry);
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
            RawMessage message = Message.decryptMessage(encrypted, keyStorageRegistry);
            chainManager.distributeMessage(message);
        }
    }

    public static @NotNull String ipString(@NotNull Socket socket) {
        return "%s:%d".formatted(socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    public @NotNull String ipString() {
        return ipString(socket);
    }

    public @NotNull Encryption serverEncryption() {
        return serverEncryption;
    }

    public @NotNull Encryption symKeyEncryption() {
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
        keyStorageRegistry.set(publicKey);
    }

    public void setClientKey(@NotNull SymmetricKeyStorage symmetricKeyStorage) {
        symKeyEncryption = symmetricKeyStorage.encryption();
        keyStorageRegistry.set(symmetricKeyStorage);
    }

    private @NotNull Encryption currentEncryption() {
        return symKeyEncryption() != Encryptions.NONE ? symKeyEncryption() : serverEncryption();
    }

    public void sendMessage(@NotNull IMessage message) throws InterruptedException {
        if (message.headersEncryption() == Encryptions.NONE)
            message.setHeadersEncryption(currentEncryption());

        sendingQueue.put(message);
    }

    public synchronized void disconnect() throws SocketClosingException, InterruptedException {

        synchronized (this) {
            connected = false;
        }

        LOGGER.info("Sending exit message");
        sendMessage(new ExitMessage());
        LOGGER.info("Disconnecting from {}", ipString(socket));

        chainManager.interruptAll();

        try {
            socket.close();
        } catch (IOException e) {
            throw new SocketClosingException("Cannot close socket", e);
        }
    }
}
