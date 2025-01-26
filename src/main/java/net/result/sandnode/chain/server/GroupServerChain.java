package net.result.sandnode.chain.server;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.group.ClientGroup;
import net.result.sandnode.group.Group;
import net.result.sandnode.message.types.GroupMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.group.GroupManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(GroupServerChain.class);

    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        GroupManager groupManager = session.server.serverConfig.groupManager();
        GroupMessage groupMessage = new GroupMessage(queue.take());

        LOGGER.debug(new String(groupMessage.getBody()));

        Set<Group> groups = groupMessage
                .getGroupNames().stream()
                .map(groupManager::getGroup)
                .collect(Collectors.toSet());

        Optional<String> opt = groupMessage.getHeaders().getOptionalValue("remove");
        boolean add = opt.isEmpty() || !opt.get().equals("true");

        if (add) session.addToGroups(groups);
        else session.removeFromGroups(groups);

        sendFin(new GroupMessage(
                session.getGroups().stream()
                .filter(ClientGroup.class::isInstance)
                .map(ClientGroup.class::cast)
                .map(ClientGroup::getName)
                .collect(Collectors.toSet())
        ));
    }
}
