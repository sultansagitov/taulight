package net.result.taulight.chain;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.util.IOControl;
import net.result.taulight.messages.OnlineMessage;
import net.result.taulight.messages.OnlineResponseMessage;

import java.util.Set;

public class TauOnlineChain extends ClientChain {
    public Set<String> members;

    public TauOnlineChain(IOControl io) {
        super(io);
    }

    @Override
    public void sync() throws ExpectedMessageException, InterruptedException, DeserializationException {
        OnlineMessage message = new OnlineMessage();
        send(message);
        IMessage response = queue.take();
        members = new OnlineResponseMessage(response).getMembers();
    }
}
