package net.result.sandnode.chain.client;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.types.GroupRequest;
import net.result.sandnode.message.types.GroupResponse;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class GroupClientChain extends ClientChain {
    private final Collection<String> groups;
    public Collection<String> groupsID;
    private boolean add = true;

    public GroupClientChain(IOController io, Collection<String> groups) {
        super(io);
        this.groups = groups;
    }

    public static GroupClientChain remove(@NotNull IOController io, Collection<String> groups) {
        GroupClientChain groupClientChain = new GroupClientChain(io, groups);
        groupClientChain.add = false;
        return groupClientChain;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        GroupRequest request = new GroupRequest(groups);
        request.headers().setValue("mode", add ? "add" : "remove");
        send(request);
        groupsID = new GroupResponse(queue.take()).getGroupsID();
    }
}
