package net.result.taulight.chain;

import net.result.sandnode.chain.HubServerChainManager;
import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.chain.receiver.*;
import net.result.taulight.message.TauMessageTypes;

public class TauHubServerChainManager {
    public static void addHandlers(ServerChainManager chainManager) {
        HubServerChainManager.addHandlers(chainManager);

        chainManager.addHandler(MessageTypes.LOGIN, TauLoginServerChain::new);
        chainManager.addHandler(MessageTypes.LOG_PASSWD, TauLogPasswdServerChain::new);
        chainManager.addHandler(TauMessageTypes.CHAT, ChatServerChain::new);
        chainManager.addHandler(TauMessageTypes.GROUP, GroupServerChain::new);
        chainManager.addHandler(TauMessageTypes.DIALOG, DialogServerChain::new);
        chainManager.addHandler(TauMessageTypes.UPSTREAM, UpstreamServerChain::new);
        chainManager.addHandler(TauMessageTypes.MESSAGE, MessageServerChain::new);
        chainManager.addHandler(TauMessageTypes.MEMBERS, MembersServerChain::new);
        chainManager.addHandler(TauMessageTypes.CODE, CodeServerChain::new);
        chainManager.addHandler(TauMessageTypes.REACTION, ReactionRequestServerChain::new);
        chainManager.addHandler(TauMessageTypes.ROLES, RoleServerChain::new);
        chainManager.addHandler(TauMessageTypes.TAU_SETTINGS, TauMemberSettingsServerChain::new);
        chainManager.addHandler(TauMessageTypes.MESSAGE_FILE, MessageFileServerChain::new);
        chainManager.addHandler(TauMessageTypes.PERMISSION, PermissionServerChain::new);
    }
}
