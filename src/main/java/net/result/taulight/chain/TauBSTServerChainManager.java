package net.result.taulight.chain;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.UnhandledMessageTypeServerChain;
import net.result.sandnode.chain.BSTServerChainManager;
import net.result.taulight.chain.receiver.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.message.TauMessageTypes;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
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
            return switch (tau) {
                case CHAT -> new ChatServerChain(session);
                case CHANNEL -> new ChannelServerChain(session);
                case DIALOG -> new DialogServerChain(session);
                case FWD_REQ -> new ForwardRequestServerChain(session);
                case FWD -> new UnhandledMessageTypeServerChain(session);
                case MESSAGE -> new MessageServerChain(session);
                case MEMBERS -> new MembersServerChain(session);
                case CHECK_CODE -> new CheckCodeServerChain(session);
                case USE_CODE -> new UseCodeServerChain(session);
                case REACTION -> new ReactionRequestServerChain(session);
                case ROLES -> new RoleServerChain(session);
            };
        }

        return super.createChain(type);
    }
}
