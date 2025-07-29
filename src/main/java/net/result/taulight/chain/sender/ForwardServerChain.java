package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.message.types.ForwardResponse;

public class ForwardServerChain extends ServerChain {
    public ForwardServerChain(Session session) {
        super(session);
    }

    public synchronized void response(ForwardResponse res)
            throws ProtocolException, InterruptedException, SandnodeErrorException, DatabaseException {
        sendAndReceive(res).expect(MessageTypes.HAPPY);
    }
}

