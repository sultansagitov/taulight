package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.message.util.MessageType;
import net.result.taulight.chain.server.ChannelServerChain;
import net.result.taulight.chain.server.ForwardRequestServerChain;
import net.result.taulight.chain.server.TaulightServerChain;
import net.result.taulight.message.TauMessageTypes;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ServerChain createChain(MessageType type) {
        if (type instanceof TauMessageTypes tau) {
            switch (tau) {
                case TAULIGHT -> {
                    return new TaulightServerChain(session);
                }
                case CHANNEL -> {
                    return new ChannelServerChain(session);
                }
                case FWD_REQ -> {
                    return new ForwardRequestServerChain(session);
                }
            }
        }

        return super.createChain(type);
    }
}
