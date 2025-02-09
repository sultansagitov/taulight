package net.result.taulight;

import net.result.sandnode.serverclient.Session;
import net.result.taulight.group.TauChatGroup;

public class TauAgentProtocol {
    public static void addMemberToGroup(Session session, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> session.member.equals(s.member))
                .forEach(s -> s.addToGroup(group));
    }
}
