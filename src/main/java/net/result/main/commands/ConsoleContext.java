package net.result.main.commands;

import net.result.taulight.chain.sender.ForwardRequestClientChain;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatInfoDTO;

import java.util.UUID;

public class ConsoleContext {
    public final SandnodeClient client;
    public final IOController io;
    public final String nickname;
    public final UUID keyID;
    public ForwardRequestClientChain chain;
    public UUID currentChat = null;
    public ChatInfoDTO chat;

    public ConsoleContext(SandnodeClient client, String nickname, UUID keyID) {
        this.client = client;
        io = client.io;
        this.nickname = nickname;
        this.keyID = keyID;
    }
}
