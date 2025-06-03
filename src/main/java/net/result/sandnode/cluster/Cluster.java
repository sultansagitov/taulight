package net.result.sandnode.cluster;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public interface Cluster {
    void add(@NotNull Session session);

    void remove(@NotNull Session session);

    boolean contains(Session s);

    String getID();
}
