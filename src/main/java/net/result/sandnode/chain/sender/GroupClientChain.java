package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.types.GroupRequest;
import net.result.sandnode.message.types.GroupResponse;
import net.result.sandnode.util.IOController;

import java.util.Collection;

public class GroupClientChain extends ClientChain {
    public GroupClientChain(IOController io) {
        super(io);
    }

    public Collection<String> remove(Collection<String> groups)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        GroupRequest request = new GroupRequest(groups);
        request.headers().setValue("mode", "remove");
        send(request);
        return new GroupResponse(queue.take()).getGroupsID();
    }

    public Collection<String> add(Collection<String> groups)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        GroupRequest request = new GroupRequest(groups);
        request.headers().setValue("mode", "add");
        send(request);
        return new GroupResponse(queue.take()).getGroupsID();
    }
}
