package net.result.sandnode.serverclient;

import net.result.sandnode.exception.*;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.receiver.ServerChainManager;
import net.result.sandnode.db.Member;
import net.result.sandnode.group.Group;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);
    private final Collection<Group> groups = new HashSet<>();
    public final SandnodeServer server;
    public final IOController io;
    public @Nullable Member member;

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
        }, "%s/Sending".formatted(io.ipString())).start();

        new Thread(() -> {
            try {
                io.receivingLoop();
            } catch (UnexpectedSocketDisconnectException ignored) {
            } catch (InterruptedException | SandnodeException e) {
                LOGGER.error("Error receiving message", e);
            }

            server.node.close();
            server.serverConfig.groupManager().removeSession(this);
            server.node.removeSession(this);
        }, "%s/Receiving".formatted(io.ipString())).start();
    }

    @Override
    public String toString() {
        return "<%s %s %s>".formatted(getClass().getSimpleName(), io.ipString(), member);
    }

    public void addToGroup(Group group) {
        if (!groups.contains(group)) {
            groups.add(group);
            group.add(this);
            LOGGER.info("{} added to group {}", this, group);
        }
    }

    public void removeFromGroup(Group group) {
        if (groups.contains(group)) {
            group.remove(this);
            groups.remove(group);
            LOGGER.info("Session {} removed from group {}", this, group);
        }
    }

    public void addToGroups(Collection<Group> groups) {
        groups.forEach(this::addToGroup);
    }

    public void removeFromGroups(Collection<Group> groups) {
        groups.forEach(this::removeFromGroup);
    }

    public Collection<Group> getGroups() {
        return groups;
    }

    public void close() {
        groups.forEach(group -> group.remove(this));
    }
}
