package net.result.sandnode.util.group;

import net.result.sandnode.server.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IGroupManager {
    void addToGroup(@NotNull String groupName, @NotNull Session session);

    void addToGroup(@NotNull Set<String> groupNames, @NotNull Session session);

    Set<Session> getSessions(@NotNull String fwd);
}
