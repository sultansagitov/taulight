package net.result.sandnode.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.exceptions.BusyChainID;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.MessageTypes;
import net.result.sandnode.server.Session;
import net.result.sandnode.chain.BSTChainManager;

public abstract class BSTServerChainManager extends BSTChainManager implements ServerChainManager {
    protected Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Chain createNew(RawMessage message) throws BusyChainID {
        Headers headers = message.getHeaders();

        ServerChain chain = (headers.getType() instanceof MessageTypes sysType) ? switch (sysType) {
            case PUB -> new PublicKeyServerChain(session);
            case SYM -> new SymKeyServerChain(session);
            case GROUP -> new GroupServerChain(session);
            case LOGIN -> new LoginServerChain(session);
            case REG -> new RegistrationServerChain(session);
            default -> defaultChain(message);
        } : defaultChain(message);

        chain.setID(headers.getChainID());
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new BusyChainID(e);
        }

        return chain;
    }
}
