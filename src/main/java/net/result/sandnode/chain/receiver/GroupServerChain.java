package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.group.Group;
import net.result.sandnode.message.types.GroupRequest;
import net.result.sandnode.message.types.GroupResponse;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.group.GroupManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(GroupServerChain.class);

    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        GroupManager groupManager = session.server.serverConfig.groupManager();
        GroupRequest request = new GroupRequest(queue.take());

        Set<Group> groups = request
                .getGroupsID().stream()
                .map(groupManager::getGroup)
                .collect(Collectors.toSet());

        Optional<String> opt = request.headers().getOptionalValue("mode");
        boolean add = opt.isEmpty() || !opt.get().equals("remove");

        if (add) session.addToGroups(groups);
        else session.removeFromGroups(groups);

        sendFin(new GroupResponse(session
                .getGroups().stream()
                .map(Group::getID)
                .collect(Collectors.toSet())
        ));
    }
}
