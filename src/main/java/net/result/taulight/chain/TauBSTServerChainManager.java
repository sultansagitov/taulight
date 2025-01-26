package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.taulight.chain.server.ForwardServerChain;
import net.result.taulight.chain.server.GetOnlineServerChain;
import net.result.taulight.chain.server.TaulightServerChain;
import net.result.taulight.message.TauMessageTypes;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ServerChain defaultChain(RawMessage message) {
        if (message.getHeaders().getType() instanceof TauMessageTypes tau) {
            switch (tau) {
                case FWD -> {
                    return new ForwardServerChain(session);
                }
                case TAULIGHT -> {
                    return new TaulightServerChain(session);
                }
            }
        }

        return new GetOnlineServerChain(session);
    }
}
