package net.result.sandnode.serverclient;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.cluster.Cluster;
import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.Logout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class Session {
    private static final Logger LOGGER = LogManager.getLogger(Session.class);
    private final Collection<Cluster> clusters = new HashSet<>();
    public final SandnodeServer server;
    public final IOController io;
    public @Nullable MemberEntity member;
    public @Nullable LoginEntity login;

    public Session(@NotNull SandnodeServer server, @NotNull IOController io) {
        this.server = server;
        this.io = io;

        ((ServerChainManager) io.chainManager).setSession(this);
    }

    public void start() {
        new Thread(() -> {
            try {
                Sender.sendingLoop(io);
            } catch (InterruptedException | SandnodeException e) {
                if (io.isConnected()) {
                    LOGGER.error("Error sending message", e);
                }

                Thread.currentThread().interrupt();
            }
        }, "%s/Sending".formatted(io.addressFromSocket())).start();

        new Thread(() -> {
            try {
                Receiver.receivingLoop(io);
            } catch (Exception e) {
                LOGGER.error("Error receiving message", e);
            }

            Logout.logout(this);

            server.removeSession(this);
        }, "%s/Receiving".formatted(io.addressFromSocket())).start();
    }

    public void addToCluster(Cluster cluster) {
        if (!clusters.contains(cluster)) {
            clusters.add(cluster);
            cluster.add(this);
            LOGGER.info("{} added to cluster {}", this, cluster);
        }
    }

    public void removeFromCluster(Cluster cluster) {
        if (clusters.contains(cluster)) {
            cluster.remove(this);
            clusters.remove(cluster);
            LOGGER.info("Session {} removed from cluster {}", this, cluster);
        }
    }

    public void addToClusters(Collection<Cluster> clusters) {
        clusters.forEach(this::addToCluster);
    }

    public void removeFromClusters(Collection<Cluster> clusters) {
        clusters.forEach(this::removeFromCluster);
    }

    public Collection<Cluster> getClusters() {
        return clusters;
    }

    public void close() {
        clusters.forEach(cluster -> cluster.remove(this));
    }

    @Override
    public String toString() {
        String simpleName = getClass().getSimpleName();
        Address address = io.addressFromSocket();
        if (member == null) return "<%s %s>".formatted(simpleName, address);
        return "<%s %s %s>".formatted(simpleName, address, member.nickname());
    }
}
