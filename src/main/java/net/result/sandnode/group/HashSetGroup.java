package net.result.sandnode.group;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class HashSetGroup implements Group {
    private final Collection<Session> sessions = new HashSet<>();

    @Override
    public Collection<Session> getSessions() {
        return sessions;
    }

    @Override
    public void add(@NotNull Session session) {
        sessions.add(session);
    }

    @Override
    public void remove(@NotNull Session session) {
        sessions.remove(session);
    }
}
