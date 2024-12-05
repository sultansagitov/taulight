package net.result.sandnode.client;

import net.result.sandnode.ClientProtocol;
import net.result.sandnode.Node;
import net.result.sandnode.config.IClientConfig;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.*;
import net.result.sandnode.messages.types.*;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.IOControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class SandnodeClient {
    private static final Logger LOGGER = LogManager.getLogger(SandnodeClient.class);

    public final Node node;
    public final IOControl io;
    private final Endpoint endpoint;
    private final Socket socket;

    public final IClientConfig clientConfig;

    public SandnodeClient(
            @NotNull Endpoint endpoint,
            @NotNull Node node,
            @NotNull NodeType nodeType,
            @NotNull IClientConfig clientConfig
    ) throws ConnectionException, OutputStreamException, InputStreamException {
        this.endpoint = endpoint;
        this.node = node;
        this.clientConfig = clientConfig;

        try {
            LOGGER.info("Connecting to {}", endpoint.toString());
            socket = new Socket(endpoint.host(), endpoint.port());
            LOGGER.info("Connection established.");
            io = new IOControl(socket, this.node, nodeType, node.globalKeyStorage);
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
        } catch (SocketClosingException e) {
            LOGGER.error("Error closing connection", e);
        }
    }

    public void setPublicKey() throws NoSuchEncryptionException, CreatingKeyException, CannotUseEncryption,
            UnexpectedSocketDisconnectException, EncryptionException, KeyStorageNotFoundException, DecryptionException,
            NoSuchMessageTypeException, FSException, MessageSerializationException, MessageWriteException {

        IAsymmetricKeyStorage publicKetFromFile = clientConfig.getPublicKey(endpoint);

        if (publicKetFromFile != null) {
            io.setMainKey(publicKetFromFile);
        } else {
            ClientProtocol.PUB(this);
            IKeyStorage keyStorage = node.globalKeyStorage.get(io.getServerMainEncryption());
            clientConfig.addKey(endpoint, Objects.requireNonNull(keyStorage));
        }
    }

    public void ignoreMessage(@NotNull IMessage response) throws UnexpectedSocketDisconnectException, EncryptionException,
            KeyStorageNotFoundException, MessageSerializationException, MessageWriteException {
        LOGGER.info("Ignoring message {}", response);
        Headers headers = new Headers();
        WarningMessage message = new WarningMessage(headers);
        io.sendMessage(message);
    }
}
