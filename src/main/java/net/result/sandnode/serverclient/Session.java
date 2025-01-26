package net.result.sandnode.serverclient;

import net.result.sandnode.exception.*;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.server.ServerChainManager;
import net.result.sandnode.db.IMember;
import net.result.sandnode.group.Group;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);
    private final Collection<Group> groups = new HashSet<>();
    public final SandnodeServer server;
    public final IOController io;
    public IMember member;

    public Session(@NotNull SandnodeServer server, @NotNull IOController io) {
        this.server = server;
        this.io = io;

        ((ServerChainManager) io.chainManager).setSession(this);

        new Thread(() -> {
            try {
                io.sendingLoop();
            } catch (InterruptedException | SandnodeException e) {
                if (io.isConnected()) {
                    LOGGER.error("Error sending message", e);
                }

                Thread.currentThread().interrupt();
            }
        }, "%s/Sending".formatted(io.getIpString())).start();

        new Thread(() -> {
            try {
                io.receivingLoop();
            } catch (InterruptedException | SandnodeException e) {
                if (io.isConnected()) {
                    LOGGER.error("Error receiving message", e);
                }
            }
        }, "%s/Receiving".formatted(io.getIpString())).start();
    }

    @Override
    public String toString() {
        return "<%s %s %s>".formatted(getClass().getSimpleName(), io.getIpString(), member);
    }

    public void addToGroup(Group group) {
        groups.add(group);
        group.add(this);
        LOGGER.info("Session {} added to group {}", this, group);
    }

    public void addToGroups(Collection<Group> groups) {
        groups.forEach(this::addToGroup);
    }

    public Collection<Group> getGroups() {
        return groups;
    }

    public void close() {
        groups.forEach(group -> group.remove(this));
    }
}
