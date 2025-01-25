package net.result.sandnode.chain.server;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.GroupMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.group.ClientNamedGroup;
import net.result.sandnode.group.GroupManager;

import java.util.Set;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain {
    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        GroupMessage groupMessage = new GroupMessage(queue.take());
        Set<String> groupNames = groupMessage.getGroupNames();
        GroupManager groupManager = session.server.serverConfig.groupManager();
        groupNames.forEach(groupName -> groupManager.getGroup(groupName).add(session));
        Set<String> collect = session.getGroups().stream()
                .filter(ClientNamedGroup.class::isInstance)
                .map(s -> ((ClientNamedGroup) s).name)
                .collect(Collectors.toSet());
        sendFin(new GroupMessage(collect));
    }
}
