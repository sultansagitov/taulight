package net.result.taulight.chain;

import net.result.sandnode.chain.HubServerChainManager;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.chain.receiver.*;
import net.result.taulight.message.TauMessageTypes;

public class TauHubServerChainManager extends HubServerChainManager {
    public TauHubServerChainManager() {
        super();
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
        if (type instanceof MessageTypes sys) {
            var chain = switch (sys) {
                case LOGIN -> new TauLoginServerChain(session);
                case LOG_PASSWD -> new TauLogPasswdServerChain(session);
                default -> null;
            };
            if (chain != null) return chain;
        }

        if (type instanceof TauMessageTypes tau) {
            var chain = switch (tau) {
                case CHAT -> new ChatServerChain(session);
                case GROUP -> new GroupServerChain(session);
                case DIALOG -> new DialogServerChain(session);
                case FWD_REQ -> new ForwardRequestServerChain(session);
                case MESSAGE -> new MessageServerChain(session);
                case MEMBERS -> new MembersServerChain(session);
                case CHECK_CODE -> new CheckCodeServerChain(session);
                case USE_CODE -> new UseCodeServerChain(session);
                case REACTION -> new ReactionRequestServerChain(session);
                case ROLES -> new RoleServerChain(session);
                case TAU_SETTINGS -> new TauMemberSettingsServerChain(session);
                case MESSAGE_FILE -> new MessageFileServerChain(session);
                case PERMISSION -> new PermissionServerChain(session);
                default -> null;
            };

            if (chain != null) return chain;
        }

        return super.createChain(type);
    }
}
