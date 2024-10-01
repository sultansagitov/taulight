package net.result.sandnode.server;

import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SandnodeServer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    private final GlobalKeyStorage serverKeyStorage;
    public final List<Session> sessionList = new ArrayList<>();
    public ServerSocket serverSocket;

    public SandnodeServer(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.serverKeyStorage = globalKeyStorage;
    }

    public void start() throws IOException {
        start(52525);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.info("Server is listening on port {}", port);
    }

    public void acceptSessions() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            Session session = new Session(socket, serverKeyStorage);
            sessionList.add(session);
            new ClientHandler(serverKeyStorage, sessionList, session).start();
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}
