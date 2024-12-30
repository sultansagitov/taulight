package net.result.sandnode.server;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.server.IServerChainManager;
import net.result.sandnode.util.db.IMember;
import net.result.sandnode.util.group.IGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);
    private final Set<IGroup> groups = new HashSet<>();
    public final SandnodeServer server;
    public final Socket socket;
    public final IOControl io;
    public final IServerChainManager chainManager;
    public IMember member;

    public Session(
            @NotNull SandnodeServer server,
            @NotNull Socket socket,
            @NotNull IServerChainManager chainManager,
            @NotNull IOControl io
    ) {
        this.server = server;
        this.socket = socket;
        this.chainManager = chainManager;
        chainManager.setSession(this);
        this.io = io;

        new Thread(() -> {
            try {
                this.io.sendingLoop();
            } catch (InterruptedException | SandnodeException e) {
                if (io.isConnected()) {
                    LOGGER.error("Error sending message", e);
                }
                Thread.currentThread().interrupt();
            }
        }, "%s/Sending".formatted(IOControl.getIP(socket))).start();

        new Thread(() -> {
            try {
                io.receivingLoop();
            } catch (InterruptedException | SandnodeException e) {
                if (io.isConnected()) {
                    LOGGER.error("Error receiving message", e);
                }
            }
        }, "%s/Receiving".formatted(IOControl.getIP(socket))).start();
    }

    @Override
    public String toString() {
        return "<%s %s %s>".formatted(getClass().getSimpleName(), IOControl.getIP(socket), member);
    }

    public void addToGroup(IGroup group) {
        groups.add(group);
    }

    public Set<IGroup> getGroups() {
        return groups;
    }
}
