package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;

public class LogoutClientChain extends ClientChain {
    public LogoutClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized void logout() throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(new EmptyMessage(new Headers().setType(MessageTypes.LOGOUT))).expect(MessageTypes.HAPPY);
    }
}
