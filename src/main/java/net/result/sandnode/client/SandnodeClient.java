package net.result.sandnode.client;

import net.result.sandnode.Node;
import net.result.sandnode.chain.client.IClientChainManager;
import net.result.sandnode.config.IClientConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.IOControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Function;

public class SandnodeClient {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeClient.class);

    public final Node node;
    public final IOControl io;
    public final Endpoint endpoint;
    public final Socket socket;

    public final IClientConfig clientConfig;

    public SandnodeClient(
            @NotNull Endpoint endpoint,
            @NotNull Node node,
            @NotNull NodeType nodeType,
            @NotNull IClientConfig clientConfig,
            @NotNull Function<IOControl, IClientChainManager> chainManager
    ) throws ConnectionException, OutputStreamException, InputStreamException {
        this.endpoint = endpoint;
        this.node = node;
        this.clientConfig = clientConfig;

        try {
            LOGGER.info("Connecting to {}", endpoint.toString());
            socket = new Socket(endpoint.host(), endpoint.port());
            LOGGER.info("Connection established.");
            Connection connection = Connection.fromType(node.type(), nodeType);
            io = new IOControl(socket, connection, node.globalKeyStorage, chainManager);

            new Thread(() -> {
                try {
                    io.sendingLoop();
                } catch (InterruptedException | SandnodeException e) {
                    if (io.isConnected()) {
                        LOGGER.error("Error sending message", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }, "Client/%s/Sending".formatted(IOControl.getIP(socket))).start();

            new Thread(() -> {
                try {
                    io.receivingLoop();
                } catch (InterruptedException | SandnodeException e) {
                    if (io.isConnected()) {
                        LOGGER.error("Error receiving message", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }, "Client/%s/Receiving".formatted(IOControl.getIP(socket))).start();

        } catch (InputStreamException | OutputStreamException e) {
            LOGGER.error("Error connecting to server", e);
            close();
            throw e;
        } catch (IOException e) {
            close();
            throw new ConnectionException("Error connecting to server", e);
        }
    }

    public void close() {
        try {
            if (socket != null) {
                io.disconnect();
                LOGGER.info("Connection closed.");
            }
        } catch (SocketClosingException | InterruptedException e) {
            LOGGER.error("Error closing connection", e);
        }
    }
}
