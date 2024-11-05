package net.result.sandnode.server;

import net.result.sandnode.Node;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.WrongNodeUsed;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.symmetric.Symmetric;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.MessageType.PUB;
import static net.result.sandnode.messages.util.MessageType.SYM;
import static net.result.sandnode.server.ServerError.UNKNOWN_ENCRYPTION;
import static net.result.sandnode.util.encryption.Encryption.NONE;

public class SandnodeServer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    public final Node node;
    private final ServerConfig serverConfig;
    public ServerSocket serverSocket;

    public SandnodeServer(Node node, ServerConfig serverConfig) {
        this.node = node;
        this.serverConfig = serverConfig;
    }

    private static void handleSYMKEY(
            @NotNull RawMessage request,
            @NotNull Session session
    ) throws NoSuchEncryptionException, CannotUseEncryption {
        Encryption encryption = Encryption.fromByte(Byte.parseByte(request.getHeaders().get("encryption")));
        ISymmetricKeyConvertor convertor = Symmetric.getKeyConvertor(encryption);
        SymmetricKeyStorage keyStorage = convertor.toKeyStorage(request.getBody());
        session.setKey(keyStorage);
    }

    public void start() throws IOException {
        start(serverConfig.getEndpoint().port);
    }

    public void start(int port) throws IOException {
        InetAddress host = Inet4Address.getByName(serverConfig.getEndpoint().host);
        serverSocket = new ServerSocket(port, Integer.MAX_VALUE, host);
        LOGGER.info("Server is listening on port {}:{}", host.getHostAddress(), port);
    }

    public void acceptSessions() {
        ExecutorService sessionExecutor = Executors.newCachedThreadPool();

        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                LOGGER.info("Client connected!");
            } catch (IOException e) {
                continue;
            }

            sessionExecutor.submit(() -> {
                LOGGER.info("Client connected {}:{}", socket.getInetAddress().getHostName(), socket.getPort());

                try {
                    RawMessage request = Session._receiveMessage(socket.getInputStream(), node.globalKeyStorage);
                    Connection conn = request.getHeaders().getConnection();
                    Session session = node.createSession(conn.getOpposite(), socket);

                    request = setSymKey(request, session);
                    LOGGER.info("Symmetric key initialized");

                    node.initSession(request.getHeaders().getConnection().getOpposite(), session);
                } catch (IOException | NoSuchEncryptionException | ReadingKeyException |
                         ExpectedMessageException | DecryptionException | NoSuchReqHandler |
                         WrongNodeUsed e) {
                    LOGGER.error("Error handling session", e);

                    try {
                        socket.close();
                    } catch (IOException ex) {
                        LOGGER.error("Error closing socket", ex);
                    }
                }
            });
        }

        sessionExecutor.shutdown();
    }

    private @NotNull RawMessage setSymKey(
            @NotNull RawMessage request,
            @NotNull Session session
    ) throws IOException, NoSuchEncryptionException, ReadingKeyException, DecryptionException, NoSuchReqHandler,
            ExpectedMessageException {
        try {
            // Optional
            if (request.getHeaders().getType() == PUB) {
                handlePUBLICKEY(request, session);
                request = session.receiveMessage();
            }

            // Required
            if (request.getHeaders().getType() != SYM)
                throw new ExpectedMessageException(SYM, request);

            handleSYMKEY(request, session);

        } catch (EncryptionException | CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    private void handlePUBLICKEY(
            @NotNull RawMessage request,
            @NotNull Session session
    ) throws IOException, EncryptionException, ReadingKeyException {
        Connection opposite = request.getHeaders().getConnection().getOpposite();
        Encryption mainEncryption = node.config.getMainEncryption();
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(opposite)
                .set(PUB)
                .set(NONE)
                .set("application/octet-stream")
                .set("encryption", "" + mainEncryption.asByte());

        RawMessage response = new RawMessage(headersBuilder);
        String string;

        try {
            AsymmetricKeyStorage keyStorage = node.globalKeyStorage.getAsymmetric(mainEncryption);
            IAsymmetricConvertor convertor = Asymmetric.getPublicConvertor(mainEncryption);
            string = convertor.toEncodedString(keyStorage);
            response.setBody(string.getBytes(US_ASCII));
            session.sendMessage(response);
            LOGGER.info("Message was sent: {}", response);

        } catch (ReadingKeyException | CannotUseEncryption e) {
            LOGGER.error("Unknown", e);
            UNKNOWN_ENCRYPTION.sendError(opposite, session);
        }
    }

    public void close() throws IOException {
        serverSocket.close();
        node.close();
    }
}
