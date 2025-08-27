package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;

public abstract class BaseServerChainManager extends BaseChainManager implements ServerChainManager {
    protected Session session;

    public BaseServerChainManager() {
        addHandler(MessageTypes.EXIT, ExitServerChain::new);
        addHandler(MessageTypes.PUB, PublicKeyServerChain::new);
        addHandler(MessageTypes.SYM, SymKeyServerChain::new);
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
        ReceiverChain chain = super.createChain(type);

        if (chain instanceof ServerChain serverChain) {
            serverChain.setSession(session);
        }

        return chain;
    }
}
