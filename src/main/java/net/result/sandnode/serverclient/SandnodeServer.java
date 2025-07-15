package net.result.sandnode.serverclient;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exception.*;
import net.result.sandnode.hubagent.Node;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SandnodeServer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    public final Node node;
    public final ServerConfig serverConfig;
    public final Container container;
    public ServerSocket serverSocket;

    private final Collection<Session> agentSessions = ConcurrentHashMap.newKeySet();
    private final Collection<Session> hubSessions = ConcurrentHashMap.newKeySet();

    private final ExecutorService sessionExecutor = Executors.newCachedThreadPool(new DaemonFactory());

    public SandnodeServer(Node node, ServerConfig serverConfig) {
        this.node = node;
        this.serverConfig = serverConfig;
        container = serverConfig.container();
    }

    public void start() throws ServerStartException {
        start(serverConfig.address().port());
    }

    public void start(int port) throws ServerStartException {
        try {
            InetAddress host = Inet4Address.getByName(serverConfig.address().host());
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

            String ip = IOController.addressFromSocket(clientSocket).toString();
            LOGGER.info("Client connected {}", ip);

            sessionExecutor.submit(() -> SessionHandler.handle(this, ip, clientSocket));
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

    @Override
    public String toString() {
        return "<%s %s>".formatted(getClass().getSimpleName(), serverSocket);
    }

    public Collection<Session> getAgents() {
        return agentSessions;
    }

    public Collection<Session> getHubs() {
        return hubSessions;
    }

    protected void addAsAgent(Session session) {
        agentSessions.add(session);
    }

    protected void addAsHub(Session session) {
        hubSessions.add(session);
    }

    public void removeSession(Session session) {
        hubSessions.remove(session);
        agentSessions.remove(session);
    }

    public Session createSession(Socket socket, @NotNull Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException {
        if (connection.getFrom() != node.type()) throw new WrongNodeUsedException(connection);

        ServerChainManager chainManager = node.createChainManager();
        IOController io = new IOController(socket, connection, node.keyStorageRegistry, chainManager);
        Session session = new Session(this, io);
        switch (connection.getTo()) {
            case AGENT -> addAsAgent(session);
            case HUB -> addAsHub(session);
        }

        return session;
    }
}
