package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;

public abstract class HubServerChainManager extends BaseServerChainManager {
    @Override
    public ReceiverChain createChain(MessageType type) {
        if (type instanceof MessageTypes sysType) {
            ReceiverChain chain = switch (sysType) {
                case CLUSTER -> new ClusterServerChain(session);
                case LOGIN -> new LoginServerChain(session);
                case REG -> new RegistrationServerChain(session);
                case LOGOUT -> new LogoutServerChain(session);
                case WHOAMI -> new WhoAmIServerChain(session);
                case NAME -> new NameServerChain(session);
                case DEK -> new DEKServerChain(session);
                case AVATAR -> new AvatarServerChain(session);
                default -> null;
            };
            if (chain != null) return chain;
        }

        return super.createChain(type);
    }
}
