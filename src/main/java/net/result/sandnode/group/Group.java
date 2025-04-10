package net.result.sandnode.group;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public interface Group {
    void add(@NotNull Session session);

    void remove(@NotNull Session session);

    String getID();
}
