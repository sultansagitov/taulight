package net.result.taulight;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.chain.ForwardServerChain;
import net.result.taulight.chain.TauHubServerChain;

import java.util.Optional;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public ServerChain defaultChain(RawMessage message) {
        Headers headers = message.getHeaders();
        Optional<String> opt = headers.getOptionalValue("chain-name");
        if (opt.isPresent() && opt.get().equals("fwd")) {
            return new ForwardServerChain(session);
        }
        return new TauHubServerChain(session);
    }
}
