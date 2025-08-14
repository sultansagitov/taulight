package net.result.taulight.util;

import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.DialogEntity;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.cluster.TauClusterManager;
import org.jetbrains.annotations.NotNull;

public class LoginUtil {
    public static void onLogin(@NotNull Session session) throws UnauthorizedException {
        TauClusterManager manager = session.server.container.get(TauClusterManager.class);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauMemberEntity tauMember = session.member.tauMember();

        for (GroupEntity group : tauMember.groups()) {
            ClusterUtil.addMemberToCluster(session, manager.getCluster(group));
        }

        for (DialogEntity dialog : tauMember.dialogs()) {
            ClusterUtil.addMemberToCluster(session, manager.getCluster(dialog));
        }
    }
}
