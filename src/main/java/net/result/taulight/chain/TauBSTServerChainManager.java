package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.TauChatManager;
import net.result.taulight.chain.server.ForwardServerChain;
import net.result.taulight.chain.server.TauHubServerChain;
import net.result.taulight.chain.server.TaulightServerChain;
import net.result.taulight.messages.TauMessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static net.result.taulight.messages.TauMessageTypes.FWD;
import static net.result.taulight.messages.TauMessageTypes.TAULIGHT;

public class TauBSTServerChainManager extends BSTServerChainManager {

    private static final Logger LOGGER = LogManager.getLogger(TauBSTServerChainManager.class);
    private final TauChatManager chatManager;

    public TauBSTServerChainManager(TauChatManager chatManager) {
        super();
        this.chatManager = chatManager;
    }

    @Override
    public ServerChain defaultChain(RawMessage message) {
        if (message.getHeaders().getType() == FWD) {
            return new ForwardServerChain(session);
        }

        if (message.getHeaders().getType() == TAULIGHT) {
            return new TaulightServerChain(session, chatManager);
        }

        return new TauHubServerChain(session);
    }
}
