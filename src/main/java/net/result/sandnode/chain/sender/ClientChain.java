package net.result.sandnode.chain.sender;

import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.Chain;

public abstract class ClientChain extends Chain {
    public ClientChain(IOController io) {
        super(io);
    }
}
