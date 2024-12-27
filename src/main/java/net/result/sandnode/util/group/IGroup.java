package net.result.sandnode.util.group;

import net.result.sandnode.server.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IGroup {

    Set<Session> getSessions();

    void add(@NotNull Session session);

}
