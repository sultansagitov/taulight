package net.result.taulight.chain;

import net.result.sandnode.chain.HubServerChainManager;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.chain.receiver.*;
import net.result.taulight.message.TauMessageTypes;

public class TauHubServerChainManager extends HubServerChainManager {
    public TauHubServerChainManager() {
        addHandler(MessageTypes.LOGIN, TauLoginServerChain::new);
        addHandler(MessageTypes.LOG_PASSWD, TauLogPasswdServerChain::new);
        addHandler(TauMessageTypes.CHAT, ChatServerChain::new);
        addHandler(TauMessageTypes.GROUP, GroupServerChain::new);
        addHandler(TauMessageTypes.DIALOG, DialogServerChain::new);
        addHandler(TauMessageTypes.FWD_REQ, ForwardRequestServerChain::new);
        addHandler(TauMessageTypes.MESSAGE, MessageServerChain::new);
        addHandler(TauMessageTypes.MEMBERS, MembersServerChain::new);
        addHandler(TauMessageTypes.CODE, CodeServerChain::new);
        addHandler(TauMessageTypes.REACTION, ReactionRequestServerChain::new);
        addHandler(TauMessageTypes.ROLES, RoleServerChain::new);
        addHandler(TauMessageTypes.TAU_SETTINGS, TauMemberSettingsServerChain::new);
        addHandler(TauMessageTypes.MESSAGE_FILE, MessageFileServerChain::new);
        addHandler(TauMessageTypes.PERMISSION, PermissionServerChain::new);
    }
}
