package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.GroupMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.group.ClientGroup;
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
        groupNames.forEach(groupName -> groupManager.getGroup(groupName).add(session));
        Set<String> collect = session.getGroups().stream()
                .filter(ClientGroup.class::isInstance)
                .map(s -> ((ClientGroup) s).name)
                .collect(Collectors.toSet());
        sendFin(new GroupMessage(collect));
    }
}
