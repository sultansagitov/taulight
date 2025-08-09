package net.result.main.commands;

import net.result.taulight.chain.sender.ForwardRequestClientChain;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatInfoDTO;

import java.util.UUID;

public class ConsoleContext {
    public final SandnodeClient client;
    public final IOController io;
    public ForwardRequestClientChain chain;
    public UUID currentChat = null;
    public ChatInfoDTO chat;

    public ConsoleContext(SandnodeClient client) {
        this.client = client;
        io = client.io();
    }

    public ForwardRequestClientChain chain() {
        if (chain == null) {
            chain = new ForwardRequestClientChain(client);
            io.chainManager.linkChain(chain);
        }
        return chain;
    }

    public void removeChain() {
        if (chain != null) {
            client.io().chainManager.removeChain(chain);
            chain = null;
        }
    }
}
