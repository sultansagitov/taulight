package net.result.taulight;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.group.TauChatGroup;

import java.util.Collection;

public class TauAgentProtocol {
    public static void addMemberToGroup(Session session, TauChatGroup group) {
        addMemberToGroup(session, session.member, group);
    }

    public static void addMemberToGroup(Session session, MemberEntity member, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> s.member != null && member.id().equals(s.member.id()))
                .forEach(s -> s.addToGroup(group));
    }

    public static void addMembersToGroup(Session session, Collection<MemberEntity> members, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> s.member != null && members.stream().anyMatch(m -> m.id().equals(s.member.id())))
                .forEach(s -> s.addToGroup(group));
    }

    public static void removeMemberFromGroup(Session session, TauChatGroup group) {
        removeMemberFromGroup(session, session.member, group);
    }

    public static void removeMemberFromGroup(Session session, MemberEntity member, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> s.member != null && member.id().equals(s.member.id()))
                .forEach(s -> s.removeFromGroup(group));
    }

    @SuppressWarnings("unused")
    public static void removeMembersFromGroup(Session session, Collection<MemberEntity> members, TauChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> s.member != null && members.stream().anyMatch(m -> m.id().equals(s.member.id())))
                .forEach(s -> s.removeFromGroup(group));
    }
}
