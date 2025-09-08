package net.result.sandnode.db;

import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Container;

public class MemberUpdater {
    private final JPAUtil jpaUtil;

    public MemberUpdater(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public void update(Session session) {
        if (session.member == null) return;
        for (Session agent : session.server.getAgents()) {
            if (agent.member != null && agent.member.equals(session.member)) {
                agent.member = jpaUtil.refresh(agent.member);
            }
        }
    }
}
