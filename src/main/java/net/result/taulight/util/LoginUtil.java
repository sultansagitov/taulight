package net.result.taulight.util;

import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.group.TauGroupManager;
import org.jetbrains.annotations.NotNull;

public class LoginUtil {
    public static void onLogin(@NotNull Session session) throws UnauthorizedException {
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        for (ChannelEntity channel : session.member.channels()) {
            TauAgentProtocol.addMemberToGroup(session, manager.getGroup(channel));
        }

        for (DialogEntity dialog : session.member.dialogs()) {
            TauAgentProtocol.addMemberToGroup(session, manager.getGroup(dialog));
        }
    }
}
