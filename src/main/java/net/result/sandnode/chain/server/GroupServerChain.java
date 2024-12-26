package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.GroupMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.group.IGroupManager;

import java.util.Set;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain {
    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public void start() throws InterruptedException, ExpectedMessageException {
        GroupMessage groupMessage = new GroupMessage(queue.take());
        Set<String> groupNames = groupMessage.getGroupNames().stream().filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        IGroupManager groupManager = session.server.serverConfig.groupManager();
        groupManager.addToGroup(groupNames, session);
        sendFin(new GroupMessage(groupManager.getGroups(session)));
    }
}
