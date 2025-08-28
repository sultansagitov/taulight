package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.*;
import net.result.sandnode.message.util.MessageTypes;

public class HubServerChainManager {
    public static void addHandlers(ServerChainManager chainManager) {
        BaseServerChainManager.addHandlers(chainManager);

        chainManager.addHandler(MessageTypes.CLUSTER, ClusterServerChain::new);
        chainManager.addHandler(MessageTypes.LOGIN, LoginServerChain::new);
        chainManager.addHandler(MessageTypes.REG, RegistrationServerChain::new);
        chainManager.addHandler(MessageTypes.LOGOUT, LogoutServerChain::new);
        chainManager.addHandler(MessageTypes.WHOAMI, WhoAmIServerChain::new);
        chainManager.addHandler(MessageTypes.NAME, NameServerChain::new);
        chainManager.addHandler(MessageTypes.DEK, DEKServerChain::new);
        chainManager.addHandler(MessageTypes.AVATAR, AvatarServerChain::new);
    }
}
