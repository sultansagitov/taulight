package net.result.taulight.util;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.cluster.ChatCluster;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ClusterUtil {
    public static void addMemberToCluster(Session session, ChatCluster cluster) {
        addMemberToCluster(session, session.member, cluster);
    }

    public static void addMemberToCluster(@NotNull Session session, MemberEntity member, ChatCluster cluster) {
        session.server
                .getAgents().stream()
                .filter(s -> member.equals(s.member))
                .forEach(s -> s.addToCluster(cluster));
    }

    public static void addMembersToCluster(
            @NotNull Session session,
            Collection<MemberEntity> members,
            ChatCluster cluster
    ) {
        session.server
                .getAgents().stream()
                .filter(s -> members.contains(s.member))
                .forEach(s -> s.addToCluster(cluster));
    }

    public static void removeMemberFromCluster(Session session, ChatCluster cluster) {
        removeMemberFromCluster(session, session.member, cluster);
    }

    public static void removeMemberFromCluster(@NotNull Session session, MemberEntity member, ChatCluster cluster) {
        session.server
                .getAgents().stream()
                .filter(s -> member.equals(s.member))
                .forEach(s -> s.removeFromCluster(cluster));
    }
}
