package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.taulight.chain.server.ChannelServerChain;
import net.result.taulight.chain.server.ForwardRequestServerChain;
import net.result.taulight.chain.server.ForwardServerChain;
import net.result.taulight.chain.server.TaulightServerChain;
import net.result.taulight.message.TauMessageTypes;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ServerChain defaultChain(RawMessage message) {
        if (message.getHeaders().getType() instanceof TauMessageTypes tau) {
            return switch (tau) {
                case FWD -> new ForwardServerChain(session);
                case TAULIGHT -> new TaulightServerChain(session);
                case CHANNEL -> new ChannelServerChain(session);
                case FWD_REQ -> new ForwardRequestServerChain(session);
            };
        }

        throw new ImpossibleRuntimeException("Unknown type of message");
    }
}
