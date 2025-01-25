package net.result.sandnode.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exception.BSTBusyPosition;
import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.chain.BSTChainManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BSTServerChainManager extends BSTChainManager implements ServerChainManager {
    private static final Logger LOGGER = LogManager.getLogger(BSTServerChainManager.class);
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

        LOGGER.info("Adding new chain {}", chain);
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new BusyChainID(e);
        }

        return chain;
    }
}
