package net.result.sandnode.cluster;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class HashSetCluster implements Cluster {
    private final Collection<Session> sessions = new HashSet<>();
    private final String id;

    public HashSetCluster(@NotNull String id) {
        this.id = id;
    }

    public Collection<Session> getSessions() {
        return sessions;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void add(@NotNull Session session) {
        sessions.add(session);
    }

    @Override
    public void remove(@NotNull Session session) {
        sessions.remove(session);
    }

    @Override
    public boolean contains(Session s) {
        return sessions.contains(s);
    }

    @Override
    public String toString() {
        return "<%s %s %d>".formatted(getClass().getSimpleName(), id, sessions.size());
    }
}
