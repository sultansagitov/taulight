package net.result.sandnode.group;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public interface GroupManager {
    Group getGroup(@NotNull String groupID);

    void removeSession(Session session);
}
