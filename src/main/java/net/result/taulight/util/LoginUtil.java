package net.result.taulight.util;

import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.group.TauGroupManager;
import org.jetbrains.annotations.NotNull;

public class LoginUtil {
    public static void onLogin(@NotNull Session session) throws UnauthorizedException {
        TauGroupManager manager = session.server.container.get(TauGroupManager.class);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauMemberEntity tauMember = session.member.tauMember();

        for (ChannelEntity channel : tauMember.channels()) {
            TauAgentProtocol.addMemberToGroup(session, manager.getGroup(channel));
        }

        for (DialogEntity dialog : tauMember.dialogs()) {
            TauAgentProtocol.addMemberToGroup(session, manager.getGroup(dialog));
        }
    }
}
