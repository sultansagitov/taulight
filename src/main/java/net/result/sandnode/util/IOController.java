package net.result.sandnode.util;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.chain.sender.ExitChain;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IOController {
    private static final Logger LOGGER = LogManager.getLogger(IOController.class);

    public final Connection connection;
    public final KeyStorageRegistry keyStorageRegistry;

    public final InputStream in;
    public final OutputStream out;
    public final Socket socket;
    public final BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<>();
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

    @NotNull
    public Encryption currentEncryption() {
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
