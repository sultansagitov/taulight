package net.result.sandnode;

import net.result.sandnode.Node;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.WrongNodeUsed;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.ClientHandler;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static net.result.sandnode.messages.util.NodeType.USER;

public abstract class User extends Node {
    public User(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }
    public User() {
        super();
    }

    @Override
    public @NotNull NodeType type() {
        return USER;
    }

    @Override
    public @NotNull Session createSession(
            @NotNull Connection opposite,
            @NotNull Socket socket
    ) throws IOException, WrongNodeUsed {
        Session session = new Session(this, socket, globalKeyStorage);
        switch (opposite) {
            case USER2HUB -> hubSessionList.add(session);
            case USER2USER -> userSessionList.add(session);
            default -> throw new WrongNodeUsed(opposite);
        };
        return session;
    }

    @Override
    public void initSession(
            @NotNull Connection opposite,
            @NotNull Session session
    ) throws WrongNodeUsed {
        switch (opposite) {
            case USER2HUB -> new ClientHandler(hubSessionList, session).start();
            case USER2USER -> new ClientHandler(userSessionList, session).start();
            default -> throw new WrongNodeUsed(opposite);
        }
    }

    @Override
    public void close() {
    }
}
