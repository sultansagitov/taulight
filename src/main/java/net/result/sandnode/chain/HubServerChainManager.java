package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageTypes;

public abstract class HubServerChainManager extends BaseServerChainManager {
    public HubServerChainManager() {
        addHandler(MessageTypes.CLUSTER, ClusterServerChain::new);
        addHandler(MessageTypes.LOGIN, LoginServerChain::new);
        addHandler(MessageTypes.REG, RegistrationServerChain::new);
        addHandler(MessageTypes.LOGOUT, LogoutServerChain::new);
        addHandler(MessageTypes.WHOAMI, WhoAmIServerChain::new);
        addHandler(MessageTypes.NAME, NameServerChain::new);
        addHandler(MessageTypes.DEK, DEKServerChain::new);
        addHandler(MessageTypes.AVATAR, AvatarServerChain::new);
    }
}
