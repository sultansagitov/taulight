package net.result.sandnode.chain;

import net.result.sandnode.util.IOController;

public abstract class ClientChain extends Chain {
    public ClientChain(IOController io) {
        super(io);
    }
}
