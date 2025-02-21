package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.taulight.chain.server.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.message.TauMessageTypes;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ServerChain createChain(MessageType type) {
        if (type instanceof MessageTypes sys) {
            switch (sys) {
                case LOGIN -> {
                    return new TauLoginServerChain(session);
                }
                case LOG_PASSWD -> {
                    return new TauLogPasswdServerChain(session);
                }
            }
        }
        if (type instanceof TauMessageTypes tau) {
            switch (tau) {
                case CHAT -> {
                    return new ChatServerChain(session);
                }
                case CHANNEL -> {
                    return new ChannelServerChain(session);
                }
                case DIRECT -> {
                    return new DirectServerChain(session);
                }
                case FWD_REQ -> {
                    return new ForwardRequestServerChain(session);
                }
                case MESSAGE -> {
                    return new MessageServerChain(session);
                }
            }
        }

        return super.createChain(type);
    }
}
