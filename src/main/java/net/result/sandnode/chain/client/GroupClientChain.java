package net.result.sandnode.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.types.GroupMessage;
import net.result.sandnode.util.IOControl;

import java.util.Set;

public class GroupClientChain extends ClientChain {
    private final Set<String> groups;
    public Set<String> groupNames;

    public GroupClientChain(IOControl io, Set<String> groups) {
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
