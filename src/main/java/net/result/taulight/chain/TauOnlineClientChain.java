package net.result.taulight.chain;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.util.IOControl;
import net.result.taulight.message.OnlineRequestMessage;
import net.result.taulight.message.OnlineResponseMessage;

import java.util.Collection;

public class TauOnlineClientChain extends ClientChain {
    public Collection<String> members;

    public TauOnlineClientChain(IOControl io) {
        super(io);
    }

    @Override
    public void sync() throws ExpectedMessageException, InterruptedException, DeserializationException {
        send(new OnlineRequestMessage());
        IMessage response = queue.take();
        members = new OnlineResponseMessage(response).getMembers();
    }
}
