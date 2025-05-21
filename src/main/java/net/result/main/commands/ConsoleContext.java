package net.result.main.commands;

import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.IOController;

import java.util.UUID;

public class ConsoleContext {
    public final SandnodeClient client;
    public final IOController io;
    public final String nickname;
    public final UUID keyID;
    public ConsoleForwardRequestClientChain chain;
    public UUID currentChat = null;

    public ConsoleContext(SandnodeClient client, String nickname, UUID keyID) {
        this.client = client;
        io = client.io;
        this.nickname = nickname;
        this.keyID = keyID;
    }
}
