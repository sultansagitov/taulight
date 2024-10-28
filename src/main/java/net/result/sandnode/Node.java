package net.result.sandnode;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.WrongNodeUsed;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public final GlobalKeyStorage globalKeyStorage;
    public final List<Session> userSessionList = new ArrayList<>();
    public final List<Session> hubSessionList = new ArrayList<>();

    public Node(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.globalKeyStorage = globalKeyStorage;
    }
    public Node() { this(new GlobalKeyStorage()); }



    public abstract @NotNull Session createSession(
            @NotNull Connection connection,
            @NotNull Socket socket
    ) throws IOException, WrongNodeUsed;

    public abstract void initSession(@NotNull Connection opposite, @NotNull Session session) throws WrongNodeUsed;

    public abstract @NotNull NodeType type();

    public abstract void close();

    public abstract void onUserMessage(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session
    ) throws NoSuchEncryptionException, ReadingKeyException, EncryptionException, IOException, NoSuchAlgorithmException;
}
