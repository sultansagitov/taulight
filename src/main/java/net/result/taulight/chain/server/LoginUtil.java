package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.group.TauChatGroup;
import net.result.taulight.group.TauGroupManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public class LoginUtil {
    private static final Logger LOGGER = LogManager.getLogger(LoginUtil.class);

    public static void onLogin(Session session, Chain chain) throws InterruptedException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        Collection<TauChat> chats;
        try {
            chats = database.getChats(session.member);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            chain.sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        for (TauChat chat : chats) {
            TauChatGroup tauChatGroup = manager.getGroup(chat);
            TauAgentProtocol.addMemberToGroup(session, tauChatGroup);
        }
    }
}
