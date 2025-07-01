package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;

public abstract class BaseServerChainManager extends BaseChainManager implements ServerChainManager {
    protected Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
        return type instanceof MessageTypes sysType ? switch (sysType) {
            case PUB -> new PublicKeyServerChain(session);
            case SYM -> new SymKeyServerChain(session);
            default -> new UnhandledMessageTypeServerChain(session);
        } : new UnhandledMessageTypeServerChain(session);
    }
}
