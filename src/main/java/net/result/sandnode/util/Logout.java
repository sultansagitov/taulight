package net.result.sandnode.util;

import net.result.sandnode.cluster.ClusterManager;
import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public class Logout {
    public static void logout(@NotNull Session session) {
        ClusterManager clusterManager = session.server.container.get(ClusterManager.class);
        clusterManager.removeSession(session);
        session.member = null;
        session.login = null;
    }
}
