package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.chain.server.TauLoginServerChain;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.chain.server.ChannelServerChain;
import net.result.taulight.chain.server.ForwardRequestServerChain;
import net.result.taulight.chain.server.ChatServerChain;
import net.result.taulight.message.TauMessageTypes;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ServerChain createChain(MessageType type) {
        if (type == MessageTypes.LOGIN) {
            return new TauLoginServerChain(session);
        }

        if (type instanceof TauMessageTypes tau) {
            switch (tau) {
                case CHAT -> {
                    return new ChatServerChain(session);
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
