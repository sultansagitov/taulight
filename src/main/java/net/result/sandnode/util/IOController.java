package net.result.sandnode.util;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.chain.sender.ExitChain;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

record SequencedRawMessage(long sequence, RawMessage message) { }

public class IOController {
    private static final Logger LOGGER = LogManager.getLogger(IOController.class);

    private final Connection connection;
    public final KeyStorageRegistry keyStorageRegistry;

    private final InputStream in;
    private final OutputStream out;
    public final Socket socket;
    private final BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<>();
    public final ChainManager chainManager;

    private Encryption serverEncryption = Encryptions.NONE;
    private Encryption symKeyEncryption = Encryptions.NONE;
    public boolean connected = true;

    public IOController(Socket socket, Connection conn, KeyStorageRegistry ksr, ChainManager chainManager)
            throws InputStreamException, OutputStreamException {
        this.in = StreamReader.inputStream(socket);
        this.out = StreamReader.outputStream(socket);
        this.socket = socket;
        connection = conn;
        keyStorageRegistry = ksr.copy();
        this.chainManager = chainManager;
    }

    private void beforeSending(Message message) {
        Headers headers = message.headers();
        headers.setConnection(connection);
        if (message.headersEncryption() == Encryptions.NONE) {
            message.setHeadersEncryption(currentEncryption());
        }
        if (headers.bodyEncryption() == Encryptions.NONE) {
            headers.setBodyEncryption(currentEncryption());
        }
        headers.setValue("random", RandomUtil.getRandomString());
    }

    public void sendingLoop() throws InterruptedException, SandnodeException {
        while (connected) {
            Message message = sendingQueue.take();
            beforeSending(message);

            Message sent = null;
            byte[] byteArray = null;
            SandnodeError error = null;
            try {
                byteArray = message.toByteArray(keyStorageRegistry);
                sent = message;
            } catch (MessageSerializationException | IllegalMessageLengthException e) {
                LOGGER.error("Serialization or message length issue", e);
                error = Errors.SERVER;
            } catch (KeyStorageNotFoundException e) {
                LOGGER.error("Key storage not found", e);
                error = Errors.KEY_NOT_FOUND;
            } catch (EncryptionException e) {
                LOGGER.error("Encryption or key issue", e);
                error = Errors.ENCRYPT;
            }

            if (error != null) {
                ErrorMessage errorMessage = new ErrorMessage(error);
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

    public void receivingLoop() throws InterruptedException {
        BlockingQueue<SequencedRawMessage> decryptedQueue;
        decryptedQueue = new PriorityBlockingQueue<>(100, Comparator.comparingLong(SequencedRawMessage::sequence));

        AtomicLong sequenceCounter = new AtomicLong(0);
        AtomicLong nextExpected = new AtomicLong(0);

        try (ExecutorServiceResource poolResource = new ExecutorServiceResource()) {
            ExecutorService decryptorPool = poolResource.executor();

            LOGGER.info("Receiving loop started");

            Thread readerThread = new Thread(() -> {
                try {
                    while (connected) {
                        EncryptedMessage encrypted;
                        synchronized (in) {
                            encrypted = EncryptedMessage.readMessage(in);
                        }

                        long seq = sequenceCounter.getAndIncrement();

                        decryptorPool.submit(() -> {
                            try {
                                RawMessage raw = MessageUtil.decryptMessage(encrypted, keyStorageRegistry);
                                decryptedQueue.put(new SequencedRawMessage(seq, raw));
                            } catch (Exception e) {
                                LOGGER.warn("Failed to decrypt message seq={}", seq, e);
                            }
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error("Reader thread encountered an error", e);
                }
            });

            Thread distributorThread = new Thread(() -> {
                try {
                    while (connected || !decryptorPool.isTerminated() || !decryptedQueue.isEmpty()) {
                        SequencedRawMessage next = decryptedQueue.peek();
                        if (next != null && next.sequence() == nextExpected.get()) {
                            decryptedQueue.poll();
                            chainManager.distributeMessage(next.message());
                            nextExpected.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Distributor thread encountered an error", e);
                }
            });

            readerThread.start();
            distributorThread.start();

            readerThread.join();
            distributorThread.join();

            LOGGER.info("Receiving loop stopped");
        }
    }

    public static @NotNull Address addressFromSocket(@NotNull Socket socket) {
        return new Address(socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    public @NotNull Address addressFromSocket() {
        return addressFromSocket(socket);
    }

    public @NotNull Encryption serverEncryption() {
        return serverEncryption;
    }

    public @NotNull Encryption symKeyEncryption() {
        return symKeyEncryption;
    }

    public synchronized boolean isConnected() {
        return socket.isConnected() && connected;
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

    public void sendMessage(@NotNull Message message) throws InterruptedException {
        if (message.headersEncryption() == Encryptions.NONE)
            message.setHeadersEncryption(currentEncryption());
        sendingQueue.put(message);
    }

    public synchronized void disconnect(boolean sendMessage)
            throws SocketClosingException, InterruptedException, UnprocessedMessagesException {
        LOGGER.info("Disconnecting from {}", addressFromSocket(socket));
        connected = false;
        LOGGER.info("Sending exit message");

        if (sendMessage) {
            ExitChain chain = new ExitChain(this);
            chainManager.linkChain(chain);
            chain.exit();
            chainManager.removeChain(chain);
        }

        chainManager.interruptAll();

        try {
            socket.close();
        } catch (IOException e) {
            throw new SocketClosingException("Cannot close socket", e);
        }
    }
}
