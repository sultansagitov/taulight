package net.result.sandnode.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.types.GroupMessage;
import net.result.sandnode.util.IOController;

import java.util.Collection;

public class GroupClientChain extends ClientChain {
    private final Collection<String> groups;
    public Collection<String> groupNames;

    public GroupClientChain(IOController io, Collection<String> groups) {
        super(io);
        this.groups = groups;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        send(new GroupMessage(groups));
        GroupMessage groupMessage = new GroupMessage(queue.take());
        groupNames = groupMessage.getGroupNames();
    }
}
