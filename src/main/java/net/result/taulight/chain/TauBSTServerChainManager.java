package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.taulight.chain.server.ForwardServerChain;
import net.result.taulight.chain.server.GetOnlineServerChain;
import net.result.taulight.chain.server.TaulightServerChain;
import net.result.taulight.message.TauMessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TauBSTServerChainManager extends BSTServerChainManager {
    private static final Logger LOGGER = LogManager.getLogger(TauBSTServerChainManager.class);

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
