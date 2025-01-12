package net.result.sandnode.chain.client;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.BSTChainManager;

public abstract class BSTClientChainManager extends BSTChainManager implements ClientChainManager {
    protected final IOControl io;

    public BSTClientChainManager(IOControl io) {
        super();
        this.io = io;
    }

    @Override
    public Chain createNew(RawMessage message) throws BSTBusyPosition {
        Headers headers = message.getHeaders();
        Chain chain = defaultChain(message);
        chain.setID(headers.getChainID());
        bst.add(chain);
        return chain;
    }


}
