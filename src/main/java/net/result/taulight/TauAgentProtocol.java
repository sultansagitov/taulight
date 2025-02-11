package net.result.taulight;

import net.result.sandnode.db.Member;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.group.TauChatGroup;

import java.util.Collection;

public class TauAgentProtocol {
    public static void addMemberToGroup(Session session, TauChatGroup group) {
        addMemberToGroup(session, session.member, group);
    }

    public static void addMemberToGroup(Session session, Member member, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> member.equals(s.member))
                .forEach(s -> s.addToGroup(group));
    }

    public static void addMembersToGroup(Session session, Collection<Member> members, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> members.stream().anyMatch(s.member::equals))
                .forEach(s -> s.addToGroup(group));
    }
}
