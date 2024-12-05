package net.result.sandnode.server;

import net.result.sandnode.Node;
import net.result.sandnode.ServerProtocol;
import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.*;
import net.result.sandnode.messages.types.RequestMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.util.StreamReader;
import net.result.sandnode.util.ctx.IContextManager;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.InMemoryDatabase;
import net.result.sandnode.util.group.GroupManager;
import net.result.sandnode.util.group.IGroupManager;
import net.result.sandnode.util.token.ITokenizer;
import net.result.sandnode.util.token.SimpleTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.result.sandnode.messages.util.MessageTypes.*;

public class SandnodeServer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    public final Node node;
    public final IServerConfig serverConfig;
    public ServerSocket serverSocket;
    public final IGroupManager groups = new GroupManager();
    public final IContextManager contextManager = null;
    public final IDatabase database = new InMemoryDatabase();
    public final ITokenizer tokenizer = new SimpleTokenizer();
    private boolean configuring = true;
    private boolean running = false;

    public SandnodeServer(Node node, IServerConfig serverConfig) {
        this.node = node;
        this.serverConfig = serverConfig;
    }

    public void start() throws ServerStartException {
        start(serverConfig.endpoint().port());
    }

    public void start(int port) throws ServerStartException {
        try {
            InetAddress host = Inet4Address.getByName(serverConfig.endpoint().host());
            serverSocket = new ServerSocket(port, Integer.MAX_VALUE, host);
            running = true;
        } catch (IOException e) {
            throw new ServerStartException("Failed to start server on port " + port, e);
        }
    }

    public void acceptSessions() throws SocketAcceptionException {
        ExecutorService sessionExecutor = Executors.newCachedThreadPool();

        while (!serverSocket.isClosed()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new SocketAcceptionException("Error accepting client socket connection", e);
            }

            String ip = IOControl.getIP(clientSocket);
            LOGGER.info("Client connected {}", ip);

            sessionExecutor.submit(() -> {
                Thread.currentThread().setName(ip);

                try {
                    InputStream inputStream = StreamReader.inputStream(clientSocket);
                    EncryptedMessage encrypted = EncryptedMessage.readMessage(inputStream);
                    IMessage request = Message.decryptMessage(node.globalKeyStorage, encrypted);
                    Connection conn = request.getHeaders().getConnection();
                    Session session = node.createSession(this, clientSocket, conn.getOpposite());
                    configureConnection(request, session);
                    request = session.io.receiveMessage();
                    conn = request.getHeaders().getConnection();
                    node.initSession(this, conn.getOpposite(), session);

                } catch (ExpectedMessageException | KeyStorageNotFoundException | NoSuchMessageTypeException |
                         WrongEncryptionException | WrongNodeUsedException | CannotUseEncryption | DecryptionException |
                         EncryptionException | NoSuchEncryptionException | UnexpectedSocketDisconnectException |
                         MessageSerializationException | MessageWriteException | IllegalMessageLengthException |
                         InputStreamException | OutputStreamException e) {
                    LOGGER.error("Error handling session for client {}: {}", ip, e.getMessage(), e);
                }

                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    LOGGER.error("Error closing socket for client {}: {}", ip, ex.getMessage(), ex);
                }
            });
        }

        sessionExecutor.shutdown();
    }

    private synchronized void configureConnection(
            @NotNull IMessage request,
            @NotNull Session session
    ) throws NoSuchEncryptionException, DecryptionException, NoSuchMessageTypeException, ExpectedMessageException,
            EncryptionException, KeyStorageNotFoundException, UnexpectedSocketDisconnectException, CannotUseEncryption,
            WrongEncryptionException, MessageSerializationException, MessageWriteException,
            IllegalMessageLengthException {
        if (request.getHeaders().getType() == REQ) {
            var req = new RequestMessage(request);
            ServerProtocol.sendPUB(session);
            request = session.io.receiveMessage();
        }

        ServerProtocol.handleSYM(session, request);

        ServerProtocol.sendAuthRequest(session);
        request = session.io.receiveMessage();

        IMessageType type = request.getHeaders().getType();

        if (type == REG) ServerProtocol.handleREG(session,request);
        else if (type == LOGIN) ServerProtocol.handleLOGIN(session,request);
        else throw new ExpectedMessageException(REG, LOGIN, request);
        configuring = false;
    }

    public synchronized boolean isConfiguring() {
        return configuring;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void close() throws ServerClosingException {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new ServerClosingException("Failed to close the server socket", e);
        }

        node.close();
        running = false;
    }


}
