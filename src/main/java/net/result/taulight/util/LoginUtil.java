package net.result.taulight.util;

import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.cluster.TauClusterManager;
import org.jetbrains.annotations.NotNull;

public class LoginUtil {
    public static void onLogin(@NotNull Session session) throws UnauthorizedException {
        TauClusterManager manager = session.server.container.get(TauClusterManager.class);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauMemberEntity tauMember = session.member.tauMember();

        for (ChannelEntity channel : tauMember.channels()) {
            TauAgentProtocol.addMemberToCluster(session, manager.getCluster(channel));
        }

        for (DialogEntity dialog : tauMember.dialogs()) {
            TauAgentProtocol.addMemberToCluster(session, manager.getCluster(dialog));
        }
    }
}
