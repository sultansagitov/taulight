package net.result.sandnode.serverclient;

import net.result.sandnode.chain.ClientChainManager;
import net.result.sandnode.config.ClientConfig;
import net.result.sandnode.exception.ConnectionException;
import net.result.sandnode.exception.InputStreamException;
import net.result.sandnode.exception.OutputStreamException;
import net.result.sandnode.hubagent.Node;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.util.Address;
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
 * This client establishes a connection to a specified address,
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
public class SandnodeClient implements Peer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeClient.class);

    private final Node node;
    private IOController io;

    public final Address address;
    public final NodeType nodeType;
    public final ClientConfig config;
    public Socket socket;

    public String nickname;

    /**
     * Constructs a new {@code SandnodeClient}.
     * <p>
     * It is recommended to use {@link #fromLink(SandnodeLinkRecord, Node, ClientConfig)}
     * to create an instance instead of calling this constructor directly.
     * </p>
     */
    public SandnodeClient(Address address, Node node, NodeType nodeType, ClientConfig config) {
        this.address = address;
        this.node = node;
        this.nodeType = nodeType;
        this.config = config;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull SandnodeClient fromLink(SandnodeLinkRecord link, Node node, ClientConfig clientConfig) {
        return new SandnodeClient(link.address(), node, link.nodeType(), clientConfig);
    }

    public void start(ClientChainManager chainManager)
            throws InputStreamException, OutputStreamException, ConnectionException {
        try {
            LOGGER.info("Connecting to {}", address);
            start(chainManager, new Socket(address.host(), address.port()));
        } catch (IOException e) {
            close();
            throw new ConnectionException("Error connecting to server", e);
        }
    }

    public void start(ClientChainManager chainManager, Socket socket)
            throws InputStreamException, OutputStreamException {
        this.socket = socket;
        try {
            LOGGER.info("Connection established.");
            Connection connection = Connection.fromType(node.type(), nodeType);
            io = new IOController(socket, connection, node.keyStorageRegistry, chainManager);

            Thread sendingThread = new Thread(() -> {
                try {
                    Sender.sendingLoop(this);
                } catch (Exception e) {
                    if (io.isConnected()) {
                        LOGGER.error("Error sending message", e);
                    }
                    Thread.currentThread().interrupt();
                }
            });
            sendingThread.setName("C/%s/Send".formatted(IOController.addressFromSocket(socket)));
            sendingThread.setDaemon(true);
            sendingThread.start();

            Thread receivingThread = new Thread(() -> {
                try {
                    Receiver.receivingLoop(io, this);
                } catch (Exception e) {
                    if (io.isConnected()) {
                        LOGGER.error("Error receiving message", e);
                    }
                    close();
                    Thread.currentThread().interrupt();
                }
            });
            receivingThread.setName("C/%s/Rec".formatted(IOController.addressFromSocket(socket)));
            receivingThread.setDaemon(true);
            receivingThread.start();

        } catch (InputStreamException | OutputStreamException e) {
            LOGGER.error("Error connecting to server", e);
            close();
            throw e;
        }
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public IOController io() {
        return io;
    }

    @Override
    public void close() {
        try {
            if (socket != null) {
                node.close();
                io.disconnect(true);
                LOGGER.info("Connection closed.");
            }
        } catch (Exception e) {
            LOGGER.error("Error closing connection", e);
        }
    }
}
