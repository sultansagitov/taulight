package net.result.sandnode.util.group;

import net.result.sandnode.server.Session;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class Group implements IGroup {
    private final Set<Session> sessions = new HashSet<>();

    @Override
    public Set<Session> getSessions() {
        return sessions;
    }

    @Override
    public void add(@NotNull Session session) {
        session.addToGroup(this);
        sessions.add(session);
    }
}
