package net.result.sandnode.group;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Group {

    Collection<Session> getSessions();

    void add(@NotNull Session session);

    void remove(@NotNull Session session);

    String getID();
}
