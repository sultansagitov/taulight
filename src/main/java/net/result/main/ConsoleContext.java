package net.result.main;

import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.sandnode.util.IOController;

import java.util.UUID;

public class ConsoleContext {
    public final ConsoleForwardRequestClientChain chain;
    public final IOController io;
    public final String nickname;
    public UUID currentChat = null;

    public ConsoleContext(ConsoleForwardRequestClientChain chain, IOController io, String nickname) {
        this.chain = chain;
        this.io = io;
        this.nickname = nickname;
    }
}
