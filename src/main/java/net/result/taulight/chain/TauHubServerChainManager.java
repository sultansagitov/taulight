package net.result.taulight.chain;

import net.result.sandnode.chain.HubServerChainManager;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.chain.receiver.*;
import net.result.taulight.message.TauMessageTypes;

public class TauHubServerChainManager extends HubServerChainManager {
    @Override
    public ServerChain createSessionChain(MessageType type) {
        if (type instanceof MessageTypes sys) {
            var chain = switch (sys) {
                case LOGIN -> new TauLoginServerChain();
                case LOG_PASSWD -> new TauLogPasswdServerChain();
                default -> null;
            };
            if (chain != null) return chain;
        }

        if (type instanceof TauMessageTypes tau) {
            var chain = switch (tau) {
                case CHAT -> new ChatServerChain();
                case GROUP -> new GroupServerChain();
                case DIALOG -> new DialogServerChain();
                case FWD_REQ -> new ForwardRequestServerChain();
                case MESSAGE -> new MessageServerChain();
                case MEMBERS -> new MembersServerChain();
                case CODE -> new CodeServerChain();
                case REACTION -> new ReactionRequestServerChain();
                case ROLES -> new RoleServerChain();
                case TAU_SETTINGS -> new TauMemberSettingsServerChain();
                case MESSAGE_FILE -> new MessageFileServerChain();
                case PERMISSION -> new PermissionServerChain();
                default -> null;
            };

            if (chain != null) return chain;
        }

        return super.createSessionChain(type);
    }
}
