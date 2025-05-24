package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.chain.receiver.DEKServerChain;

public abstract class BSTServerChainManager extends BSTChainManager implements ServerChainManager {
    protected Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
        return (type instanceof MessageTypes sysType) ? switch (sysType) {
            case PUB -> new PublicKeyServerChain(session);
            case SYM -> new SymKeyServerChain(session);
            case GROUP -> new GroupServerChain(session);
            case LOGIN -> new LoginServerChain(session);
            case REG -> new RegistrationServerChain(session);
            case LOGOUT -> new LogoutServerChain(session);
            case WHOAMI -> new WhoAmIServerChain(session);
            case NAME -> new NameServerChain(session);
            case DEK -> new DEKServerChain(session);
            default -> new UnhandledMessageTypeServerChain(session);
        } : new UnhandledMessageTypeServerChain(session);
    }
}
