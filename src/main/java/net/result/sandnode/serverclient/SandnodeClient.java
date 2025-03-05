package net.result.sandnode.serverclient;

import net.result.sandnode.hubagent.Node;
import net.result.sandnode.chain.client.ClientChainManager;
import net.result.sandnode.config.ClientConfig;
import net.result.sandnode.exception.*;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.IOController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;

/**
 * Represents a client that connects to a Sandnode server.
 * <p>
 * This client establishes a connection to a specified endpoint,
 * manages communication with the server, and handles sending
 * and receiving messages using an {@link IOController}.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>{@code
 * SandnodeLinkRecord link = ...;
 * TauAgent agent = new TauAgent();
 * ClientConfig clientConfig = new ClientPropertiesConfig();
 * SandnodeClient client = SandnodeClient.fromLink(link, agent, clientConfig);
 *
 * ConsoleClientChainManager chainManager = new ConsoleClientChainManager();
 * client.start(chainManager);
 * }</pre>
 */
public class SandnodeClient {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeClient.class);

    public final Endpoint endpoint;
    public final Node node;
    public final NodeType nodeType;
    public final ClientConfig clientConfig;

    public IOController io;
    public Socket socket;

    /**
     * Constructs a new {@code SandnodeClient}.
     * <p>
     * It is recommended to use {@link #fromLink(SandnodeLinkRecord, Node, ClientConfig)}
     * to create an instance instead of calling this constructor directly.
     * </p>
     */
    public SandnodeClient(Endpoint endpoint, Node node, NodeType nodeType, ClientConfig clientConfig) {
        this.endpoint = endpoint;
        this.node = node;
        this.nodeType = nodeType;
        this.clientConfig = clientConfig;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull SandnodeClient fromLink(SandnodeLinkRecord link, Node node, ClientConfig clientConfig) {
        return new SandnodeClient(link.endpoint(), node, link.nodeType(), clientConfig);
    }

    public void start(ClientChainManager chainManager)
            throws InputStreamException, OutputStreamException, ConnectionException {
        try {
            LOGGER.info("Connecting to {}", endpoint.toString());
            socket = new Socket(endpoint.host(), endpoint.port());
            LOGGER.info("Connection established.");
            Connection connection = Connection.fromType(node.type(), nodeType);
            io = new IOController(socket, connection, node.keyStorageRegistry, chainManager);
            chainManager.setIOController(io);

            new Thread(() -> {
                try {
                    io.sendingLoop();
                } catch (InterruptedException | SandnodeException e) {
                    if (io.isConnected()) {
                        LOGGER.error("Error sending message", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }, "Client/%s/Sending".formatted(IOController.ipString(socket))).start();

            new Thread(() -> {
                try {
                    io.receivingLoop();
                } catch (InterruptedException | SandnodeException e) {
                    if (io.isConnected()) {
                        LOGGER.error("Error receiving message", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }, "Client/%s/Receiving".formatted(IOController.ipString(socket))).start();

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
                node.close();
                io.disconnect();
                LOGGER.info("Connection closed.");
            }
        } catch (SocketClosingException | InterruptedException e) {
            LOGGER.error("Error closing connection", e);
        }
    }
}
