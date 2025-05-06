package net.result.taulight;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.group.ChatGroup;

import java.util.Collection;

public class TauAgentProtocol {
    public static void addMemberToGroup(Session session, ChatGroup group) {
        addMemberToGroup(session, session.member, group);
    }

    public static void addMemberToGroup(Session session, MemberEntity member, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> member == s.member)
                .forEach(s -> s.addToGroup(group));
    }

    public static void addMembersToGroup(Session session, Collection<MemberEntity> members, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> members.contains(s.member))
                .forEach(s -> s.addToGroup(group));
    }

    public static void removeMemberFromGroup(Session session, ChatGroup group) {
        removeMemberFromGroup(session, session.member, group);
    }

    public static void removeMemberFromGroup(Session session, MemberEntity member, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> member == s.member)
                .forEach(s -> s.removeFromGroup(group));
    }

    public static void removeMembersFromGroup(Session session, Collection<MemberEntity> members, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> members.contains(s.member))
                .forEach(s -> s.removeFromGroup(group));
    }
}
