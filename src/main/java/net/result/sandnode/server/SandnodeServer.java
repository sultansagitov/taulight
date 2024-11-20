package net.result.sandnode.server;

import net.result.sandnode.Hub;
import net.result.sandnode.Node;
import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.link.Links;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.interfaces.*;
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
import static net.result.sandnode.messages.util.MessageTypes.PUB;
import static net.result.sandnode.messages.util.MessageTypes.SYM;
import static net.result.sandnode.server.ServerError.UNKNOWN_ENCRYPTION;
import static net.result.sandnode.util.encryption.Encryption.NONE;

public class SandnodeServer {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeServer.class);
    public final Node node;
    private final IServerConfig serverConfig;
    public ServerSocket serverSocket;

    public SandnodeServer(Node node, IServerConfig serverConfig) {
        this.node = node;
        this.serverConfig = serverConfig;
    }

    private static void handleSYMKEY(
            @NotNull RawMessage request,
            @NotNull Session session
    ) throws NoSuchEncryptionException, CannotUseEncryption {
        ISymmetricEncryption encryption =
                Encryptions.findSymmetric(Byte.parseByte(request.getHeaders().get("encryption")));
        ISymmetricKeyConvertor convertor = encryption.keyConvertor();
        ISymmetricKeyStorage keyStorage = convertor.toKeyStorage(request.getBody());
        session.setKey(keyStorage);
        LOGGER.info("Symmetric key initialized");
    }

    public void start() throws IOException, ReadingKeyException, KeyStorageNotFoundException {
        start(serverConfig.getEndpoint().port);
    }

    public void start(int port) throws IOException, ReadingKeyException, KeyStorageNotFoundException {
        InetAddress host = Inet4Address.getByName(serverConfig.getEndpoint().host);
        serverSocket = new ServerSocket(port, Integer.MAX_VALUE, host);
        String link = Links.toString((Hub) node, serverConfig.getEndpoint(), node.config.getMainEncryption());
        System.out.println("Link for server:");
        System.out.println();
        System.out.println(link);
        System.out.println();
    }

    public void acceptSessions() {
        ExecutorService sessionExecutor = Executors.newCachedThreadPool();

        while (!serverSocket.isClosed()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                LOGGER.info("Client connected!");
            } catch (IOException e) {
                continue;
            }

            sessionExecutor.submit(() -> {
                LOGGER.info("Client connected {}:{}", clientSocket.getInetAddress().getHostName(), clientSocket.getPort());

                try {
                    Message.EncryptedMessage encrypted = Message.readMessage(clientSocket.getInputStream());
                    RawMessage request = Message.decryptMessage(node.globalKeyStorage, encrypted);
                    Connection conn = request.getHeaders().getConnection();
                    Session session = node.createSession(conn.getOpposite(), clientSocket);
                    request = setSymKey(request, session);
                    conn = request.getHeaders().getConnection();
                    node.initSession(conn.getOpposite(), session);

                } catch (Exception e) {
                    LOGGER.error("Error handling session", e);
                }

                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    LOGGER.error("Error closing socket", ex);
                }
            });
        }

        sessionExecutor.shutdown();
    }

    private @NotNull RawMessage setSymKey(
            @NotNull RawMessage request,
            @NotNull Session session
    ) throws IOException, NoSuchEncryptionException, ReadingKeyException, DecryptionException, NoSuchReqHandler,
            ExpectedMessageException, EncryptionException, KeyStorageNotFoundException, UnexpectedSocketDisconnect,
            CannotUseEncryption, WrongEncryptionException {
        // Optional
        if (request.getHeaders().getType() == PUB) {
            handlePUBLICKEY(request, session);
            request = session.receiveMessage();
        }

        // Required
        if (request.getHeaders().getType() != SYM)
            throw new ExpectedMessageException(SYM, request);

        handleSYMKEY(request, session);


        return request;
    }

    private void handlePUBLICKEY(
            @NotNull RawMessage request,
            @NotNull Session session
    ) throws IOException, EncryptionException, ReadingKeyException, KeyStorageNotFoundException,
            WrongEncryptionException {
        Connection opposite = request.getHeaders().getConnection().getOpposite();
        IAsymmetricEncryption mainEncryption = node.config.getMainEncryption();
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(opposite)
                .set(PUB)
                .set(NONE)
                .set("encryption", "" + mainEncryption.asByte());

        RawMessage response = new RawMessage(headersBuilder);
        String string;

        try {
            IAsymmetricKeyStorage keyStorage = node.globalKeyStorage.getAsymmetric(mainEncryption);
            IAsymmetricConvertor convertor = mainEncryption.publicKeyConvertor();
            string = convertor.toEncodedString(keyStorage);
            response.setBody(string.getBytes(US_ASCII));
            session.sendMessage(response);
            LOGGER.info("Message was sent: {}", response);

        } catch (ReadingKeyException e) {
            LOGGER.error("Unknown", e);
            UNKNOWN_ENCRYPTION.sendError(opposite, session);
        }
    }

    public void close() throws IOException {
        serverSocket.close();
        node.close();
    }
}
