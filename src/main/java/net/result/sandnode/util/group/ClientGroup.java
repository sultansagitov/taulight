package net.result.sandnode.util.group;

import net.result.sandnode.server.Session;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ClientGroup implements IGroup {
    public final String id;
    private final Set<Session> sessions = new HashSet<>();

    public ClientGroup(@NotNull String id) {
        this.id = id;
    }

    @Override
    public Set<Session> getSessions() {
        return sessions;
    }

    @Override
    public void add(@NotNull Session session) {
        sessions.add(session);
    }
}
