package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;

public abstract class HubServerChainManager extends BaseServerChainManager {
    @Override
    public ServerChain createSessionChain(MessageType type) {
        if (type instanceof MessageTypes sysType) {
            ServerChain chain = switch (sysType) {
                case CLUSTER -> new ClusterServerChain();
                case LOGIN -> new LoginServerChain();
                case REG -> new RegistrationServerChain();
                case LOGOUT -> new LogoutServerChain();
                case WHOAMI -> new WhoAmIServerChain();
                case NAME -> new NameServerChain();
                case DEK -> new DEKServerChain();
                case AVATAR -> new AvatarServerChain();
                default -> null;
            };
            if (chain != null) return chain;
        }

        return super.createSessionChain(type);
    }
}
