package net.result.sandnode.chain.server;

import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.group.TauChatGroup;
import net.result.taulight.group.TauGroupManager;

import java.util.Collection;

public class TauLoginServerChain extends LoginServerChain {
    public TauLoginServerChain(Session session) {
        super(session);
    }

    @Override
    protected void onLogin() {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        Collection<TauChat> chats = database.getChats(session.member);

        for (TauChat chat : chats) {
            TauChatGroup tauChatGroup = manager.getGroup(chat);
            TauAgentProtocol.addMemberToGroup(session, tauChatGroup);
        }
    }
}
