package net.result.sandnode.util;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public class Logout {
    public static void logout(@NotNull Session session) {
        session.server.serverConfig.groupManager().removeSession(session);
        session.member = null;
    }
}
