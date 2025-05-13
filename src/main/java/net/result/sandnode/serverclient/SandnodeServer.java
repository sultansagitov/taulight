package net.result.sandnode.serverclient;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.hubagent.Node;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.*;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.StreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SandnodeServer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    public final Node node;
    public final ServerConfig serverConfig;
    public final Container container;
    public ServerSocket serverSocket;

    private final ExecutorService sessionExecutor = Executors.newCachedThreadPool();

    public SandnodeServer(Node node, ServerConfig serverConfig) {
        this.node = node;
        this.serverConfig = serverConfig;
        container = serverConfig.container();
    }

    public void start() throws ServerStartException {
        start(serverConfig.endpoint().port());
    }

    public void start(int port) throws ServerStartException {
        try {
            InetAddress host = Inet4Address.getByName(serverConfig.endpoint().host());
            serverSocket = new ServerSocket(port, Integer.MAX_VALUE, host);
        } catch (IOException e) {
            throw new ServerStartException("Failed to start server on port " + port, e);
        }
    }

    public void acceptSessions() throws SocketAcceptException {
        while (!serverSocket.isClosed()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new SocketAcceptException("Error accepting client socket connection", e);
            }

            String ip = IOController.ipString(clientSocket);
            LOGGER.info("Client connected {}", ip);

            sessionExecutor.submit(() -> {
                Thread.currentThread().setName(ip);

                try {
                    InputStream inputStream = StreamReader.inputStream(clientSocket);
                    EncryptedMessage encrypted = EncryptedMessage.readMessage(inputStream);
                    RawMessage request = Message.decryptMessage(encrypted, node.keyStorageRegistry);
                    Connection conn = request.headers().connection();
                    Session session = node.createSession(this, clientSocket, conn.getOpposite());
                    session.io.chainManager.distributeMessage(request);

                    while (session.io.socket.isConnected()) {
                        Thread.onSpinWait();
                    }

                    if (session.io.socket.isConnected()) {
                        try {
                            session.io.disconnect();
                        } catch (SocketClosingException e) {
                            LOGGER.error("Error while closing socket", e);
                        }
                    }

                    node.removeHub(session);
                    node.removeAgent(session);

                    session.close();

                    LOGGER.info("Client disconnected");

                } catch (SandnodeException | InterruptedException e) {
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

    public synchronized void close() throws ServerClosingException {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new ServerClosingException("Failed to close the server socket", e);
        }

        node.close();

        container.get(JPAUtil.class).shutdown();
    }

    public synchronized void closeWithoutDBShutdown() throws ServerClosingException {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new ServerClosingException("Failed to close the server socket", e);
        }

        node.close();
    }
}
