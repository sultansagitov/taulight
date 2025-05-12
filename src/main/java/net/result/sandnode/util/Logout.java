package net.result.sandnode.util;

import net.result.sandnode.group.GroupManager;
import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public class Logout {
    public static void logout(@NotNull Session session) {
        GroupManager groupManager = session.server.container.get(GroupManager.class);
        groupManager.removeSession(session);
        session.member = null;
    }
}
