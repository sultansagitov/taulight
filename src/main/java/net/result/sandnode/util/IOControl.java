package net.result.sandnode.util;

import net.result.sandnode.chain.client.IClientChainManager;
import net.result.sandnode.chain.server.IServerChainManager;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.EncryptedMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.ExitMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.chain.IChainManager;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.result.sandnode.encryption.Encryption.NONE;

public class IOControl {
    private static final Logger LOGGER = LogManager.getLogger(IOControl.class);

    private final Connection connection;
    public final GlobalKeyStorage globalKeyStorage;

    private final InputStream in;
    private final OutputStream out;
    private final Socket socket;
    private final BlockingQueue<IMessage> sendingQueue = new LinkedBlockingQueue<>();
    public final IChainManager chainManager;

    private IEncryption serverEncryption = NONE;
    private IEncryption symKeyEncryption = NONE;
    private boolean isConnected = true;

    public IOControl(
            Socket socket,
            Connection connection,
            GlobalKeyStorage globalKeyStorage,
            Function<IOControl, IClientChainManager> chainManager
    ) throws InputStreamException, OutputStreamException {
        this.in = StreamReader.inputStream(socket);
        this.out = StreamReader.outputStream(socket);
        this.socket = socket;
        this.connection = connection;
        this.globalKeyStorage = globalKeyStorage;
        this.chainManager = chainManager.apply(this);
    }

    public IOControl(
            Socket socket,
            Connection connection,
            GlobalKeyStorage globalKeyStorage,
            IServerChainManager chainManager
    ) throws InputStreamException, OutputStreamException {
        this.in = StreamReader.inputStream(socket);
        this.out = StreamReader.outputStream(socket);
        this.socket = socket;
        this.connection = connection;
        this.globalKeyStorage = globalKeyStorage.copy();
        this.chainManager = chainManager;
    }

    public void sendingLoop() throws InterruptedException, EncryptionException, KeyStorageNotFoundException,
            MessageSerializationException, IllegalMessageLengthException, WrongKeyException, MessageWriteException,
            UnexpectedSocketDisconnectException {
        while (true) {
            IMessage message = sendingQueue.take();
            if (isConnected) {
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

                byte[] byteArray = message.toByteArray(globalKeyStorage);
                synchronized (out) {
                    try {
                        out.write(byteArray);
                        out.flush();
                    } catch (IOException e) {
                        throw new MessageWriteException(message, "Failed to write message to output.", e);
                    }
                }

                LOGGER.info("Message sent: {}", message);
            } else {
                throw new UnexpectedSocketDisconnectException("unknown");
            }
        }
    }

    public void receivingLoop() throws UnexpectedSocketDisconnectException, DecryptionException,
            NoSuchMessageTypeException, NoSuchEncryptionException, KeyStorageNotFoundException,
            WrongKeyException, InterruptedException, PrivateKeyNotFoundException {
        while (isConnected) {
            EncryptedMessage encrypted = EncryptedMessage.readMessage(in);
            RawMessage message = Message.decryptMessage(encrypted, globalKeyStorage);
            chainManager.distributeMessage(message);
        }
    }

    public static @NotNull String getIP(@NotNull Socket socket) {
        return "%s:%d".formatted(socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    public @NotNull IEncryption getServerEncryption() {
        return serverEncryption;
    }

    public @NotNull IEncryption getSymKeyEncryption() {
        return symKeyEncryption;
    }

    public synchronized boolean isConnected() {
        if (socket == null || !socket.isConnected()) {
            return false;
        }

        synchronized (this) {
            return isConnected;
        }
    }

    public void setServerKey(@NotNull IAsymmetricKeyStorage publicKey) {
        serverEncryption = publicKey.encryption();
        globalKeyStorage.set(publicKey);
    }

    public void setClientKey(@NotNull ISymmetricKeyStorage symmetricKeyStorage) {
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
            isConnected = false;
        }

        LOGGER.info("Sending exit message");
        sendMessage(new ExitMessage());
        LOGGER.info("Disconnecting from {}", getIP(socket));
        try {
            socket.close();
        } catch (IOException e) {
            throw new SocketClosingException("Cannot close socket", e);
        }
    }
}
