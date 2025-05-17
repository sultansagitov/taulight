package net.result.taulight.util;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.group.ChatGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TauAgentProtocol {
    public static void addMemberToGroup(Session session, ChatGroup group) {
        addMemberToGroup(session, session.member, group);
    }

    public static void addMemberToGroup(@NotNull Session session, MemberEntity member, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> member.equals(s.member))
                .forEach(s -> s.addToGroup(group));
    }

    public static void addMembersToGroup(@NotNull Session session, Collection<MemberEntity> members, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> members.contains(s.member))
                .forEach(s -> s.addToGroup(group));
    }

    public static void removeMemberFromGroup(Session session, ChatGroup group) {
        removeMemberFromGroup(session, session.member, group);
    }

    public static void removeMemberFromGroup(@NotNull Session session, MemberEntity member, ChatGroup group) {
        session.server.node
                .getAgents().stream()
                .filter(s -> member.equals(s.member))
                .forEach(s -> s.removeFromGroup(group));
    }
}
