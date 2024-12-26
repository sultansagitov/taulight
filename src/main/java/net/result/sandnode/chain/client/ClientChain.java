package net.result.sandnode.chain.client;

import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.Chain;

public abstract class ClientChain extends Chain {
    public ClientChain(IOControl io) {
        super(io);
    }
}
