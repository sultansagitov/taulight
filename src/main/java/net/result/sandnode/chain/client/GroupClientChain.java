package net.result.sandnode.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.types.GroupMessage;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class GroupClientChain extends ClientChain {
    private final Collection<String> groups;
    public Collection<String> groupNames;
    private boolean remove = false;

    public GroupClientChain(IOController io, Collection<String> groups) {
        super(io);
        this.groups = groups;
    }

    public static GroupClientChain remove(@NotNull IOController io, Collection<String> groups) {
        GroupClientChain groupClientChain = new GroupClientChain(io, groups);
        groupClientChain.remove = true;
        return groupClientChain;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        GroupMessage request = new GroupMessage(groups);
        if (remove) request.getHeaders().setValue("remove", "true");
        send(request);
        GroupMessage groupMessage = new GroupMessage(queue.take());
        groupNames = groupMessage.getGroupNames();
    }
}
