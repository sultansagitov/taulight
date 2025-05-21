package net.result.main.commands;

import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.sandnode.util.IOController;

import java.util.UUID;

public class ConsoleContext {
    public final IOController io;
    public final String nickname;
    public final UUID keyID;
    public ConsoleForwardRequestClientChain chain;
    public UUID currentChat = null;

    public ConsoleContext(IOController io, String nickname, UUID keyID) {
        this.io = io;
        this.nickname = nickname;
        this.keyID = keyID;
    }
}
