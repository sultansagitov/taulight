package net.result.sandnode.server;

import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SandnodeServer {

    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    private final GlobalKeyStorage globalKeyStorage;

    public SandnodeServer(@NotNull GlobalKeyStorage globalKeyStorage) throws IOException {
        this.globalKeyStorage = globalKeyStorage;
        start();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(52525)) {
            LOGGER.info("Server is listening on port 52525");

            while (true) {
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, globalKeyStorage).start();
            }
        }
    }

}
