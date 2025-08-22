package net.result.taulight.util;

import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.entity.TauMemberEntity;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class LoginUtil {
    public static void onLogin(@NotNull Session session) {
        TauClusterManager manager = session.server.container.get(TauClusterManager.class);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauMemberEntity tauMember = session.member.getTauMember();

        Stream
                .concat(tauMember.getGroups().stream(), tauMember.getDialogs().stream())
                .forEach(chat -> ClusterUtil.addMemberToCluster(session, manager.getCluster(chat)));
    }
}
