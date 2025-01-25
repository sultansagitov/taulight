package net.result.sandnode.chain.server;

import net.result.sandnode.exception.*;
import net.result.sandnode.group.ClientGroup;
import net.result.sandnode.message.types.GroupMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.group.GroupManager;

import java.util.Collection;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain {
    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        GroupMessage groupMessage = new GroupMessage(queue.take());
        Collection<String> groupNames = groupMessage.getGroupNames();
        GroupManager groupManager = session.server.serverConfig.groupManager();
        for (String groupName : groupNames) {
            groupManager.getGroup(groupName).add(session);
        }
        Collection<String> collect = session.getGroups().stream()
                .filter(ClientGroup.class::isInstance)
                .map(s -> ((ClientGroup) s).getName())
                .collect(Collectors.toSet());
        sendFin(new GroupMessage(collect));
    }
}
