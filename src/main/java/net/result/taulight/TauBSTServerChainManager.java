package net.result.taulight;

import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.chain.server.BSTServerChainManager;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.chain.ForwardServerChain;
import net.result.taulight.chain.TauHubServerChain;

public class TauBSTServerChainManager extends BSTServerChainManager {
    public TauBSTServerChainManager() {
        super();
    }

    @Override
    public IChain defaultChain(RawMessage message) {
        Headers headers = message.getHeaders();
        if (headers.hasValue("chain-name") && headers.getValue("chain-name").equals("fwd")) {
            return new ForwardServerChain(session);
        }
        return new TauHubServerChain(session);
    }
}
