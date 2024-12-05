package net.result.sandnode;

import net.result.sandnode.config.HubPropertiesConfig;
import net.result.sandnode.config.INodeConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.FSException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public final GlobalKeyStorage globalKeyStorage;
    public final List<Session> agentSessionList = new ArrayList<>();
    public final List<Session> hubSessionList = new ArrayList<>();
    public final INodeConfig nodeConfig;

    public Node(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull INodeConfig nodeConfig) {
        this.globalKeyStorage = globalKeyStorage;
        this.nodeConfig = nodeConfig;
    }

    public Node() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption, FSException {
        this(new GlobalKeyStorage(), new HubPropertiesConfig());
    }


    public abstract @NotNull Session createSession(
            @NotNull SandnodeServer server,
            @NotNull Socket socket,
            @NotNull Connection connection
    ) throws WrongNodeUsedException, OutputStreamException, InputStreamException;

    public abstract void initSession(
            @NotNull SandnodeServer server,
            @NotNull Connection opposite,
            @NotNull Session session
    ) throws WrongNodeUsedException;

    public abstract @NotNull NodeType type();

    public abstract void close();

    public abstract void onAgentMessage(
            @NotNull IMessage request,
            @NotNull Session session
    ) throws EncryptionException, KeyStorageNotFoundException, MessageSerializationException, MessageWriteException, IllegalMessageLengthException, UnexpectedSocketDisconnectException;
}
