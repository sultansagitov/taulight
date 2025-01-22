package net.result.sandnode.group;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Group {

    Set<Session> getSessions();

    void add(@NotNull Session session);

}
