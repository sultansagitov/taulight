package net.result.main.commands;

import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatInfoDTO;

import java.util.UUID;

public class ConsoleContext {
    public final SandnodeClient client;
    public final IOController io;
    public UUID currentChat = null;
    public ChatInfoDTO chat;

    public ConsoleContext(SandnodeClient client) {
        this.client = client;
        io = client.io();
    }
}
