package net.result.sandnode.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.server.Session;
import net.result.sandnode.chain.BSTChainManager;

public abstract class BSTServerChainManager extends BSTChainManager implements IServerChainManager {
    protected Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Chain createNew(RawMessage message) throws BSTBusyPosition {
        Headers headers = message.getHeaders();

        Chain chain = headers.getType() instanceof MessageType sysType ? switch (sysType) {
            case PUB -> new PublicKeyServerChain(session);
            case SYM -> new SymKeyServerChain(session);
            case GROUP -> new GroupServerChain(session);
            case REG, LOGIN -> new AuthServerChain(session);
            default -> defaultChain(message);
        } : defaultChain(message);

        chain.setID(headers.getChainID());
        bst.add(chain);

        return chain;
    }

}
