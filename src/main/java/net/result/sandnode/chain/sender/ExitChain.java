package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.BaseChain;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.types.ExitMessage;
import net.result.sandnode.util.IOController;

public class ExitChain extends BaseChain {
    public ExitChain(IOController io) {
        super(io);
    }

    public void exit() throws UnprocessedMessagesException, InterruptedException {
        send(new ExitMessage());
    }
}
