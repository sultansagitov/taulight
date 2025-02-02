package net.result.sandnode.chain.client;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exception.BSTBusyPosition;
import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.BSTChainManager;

public abstract class BSTClientChainManager extends BSTChainManager implements ClientChainManager {
    protected IOController io;

    public BSTClientChainManager() {
        super();
    }

    @Override
    public Chain createNew(RawMessage message) throws BusyChainID {
        Headers headers = message.getHeaders();
        Chain chain = defaultChain(message);
        chain.setID(headers.getChainID());
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new BusyChainID(e);
        }
        return chain;
    }

    @Override
    public void setIOController(IOController io) {
        this.io = io;
    }
}
