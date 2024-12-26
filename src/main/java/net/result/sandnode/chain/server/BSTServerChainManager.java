package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.server.Session;
import net.result.sandnode.chain.BSTChainManager;
import net.result.sandnode.chain.IChain;

public abstract class BSTServerChainManager extends BSTChainManager implements IServerChainManager {
    protected Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public IChain createNew(RawMessage message) throws BSTBusyPosition {
        Headers headers = message.getHeaders();

        IChain chain = headers.getType() instanceof MessageType sysType ? switch (sysType) {
            case PUB -> new PublicKeyServerChain(session);
            case SYM -> new SymKeyServerChain(session);
            case GROUP -> new GroupServerChain(session);
            case REG -> new AuthServerChain(session);
            default -> defaultChain(message);
        } : defaultChain(message);

        chain.setID(headers.getChainID());
        bst.add(chain);

        if (headers.hasValue("chain-name"))
            chainMap.put(headers.getValue("chain-name"), chain);

        return chain;
    }

}
