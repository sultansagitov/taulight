package net.result.taulight.util;

import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.repository.TauMemberRepository;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class LoginUtil {
    public static void onLogin(@NotNull Session session) {
        TauClusterManager manager = session.server.container.get(TauClusterManager.class);
        TauMemberRepository tauMemberRepo = session.server.container.get(TauMemberRepository.class);
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauMemberEntity tauMember = tauMemberRepo.findByMember(session.member);

        Stream
                .concat(tauMember.getGroups().stream(), tauMember.getDialogs().stream())
                .forEach(chat -> ClusterUtil.addMemberToCluster(session, manager.getCluster(chat)));
    }
}
