package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.BaseChain;
import net.result.sandnode.message.types.ExitMessage;
import net.result.sandnode.util.IOController;

public class ExitChain extends BaseChain {
    public ExitChain(IOController io) {
        setIO(io);
    }

    public void exit() {
        send(new ExitMessage());
    }
}
