package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.GroupMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.group.ClientNamedGroup;
import net.result.sandnode.util.group.GroupManager;

import java.util.Set;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain {
    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        GroupMessage groupMessage = new GroupMessage(queue.take());
        Set<String> groupNames = groupMessage.getGroupNames().stream().filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        GroupManager groupManager = session.server.serverConfig.groupManager();
        groupNames.forEach(groupName -> groupManager.getGroup(groupName).add(session));
        Set<String> collect = session.getGroups().stream()
                .filter(ClientNamedGroup.class::isInstance)
                .map(s -> ((ClientNamedGroup) s).name)
                .collect(Collectors.toSet());
        sendFin(new GroupMessage(collect));
    }
}
