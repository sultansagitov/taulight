package net.result.main.chain;

import net.result.main.chain.receiver.ConsoleForwardClientChain;
import net.result.main.chain.receiver.ConsoleReactionResponseClientChain;
import net.result.sandnode.chain.BaseClientChainManager;
import net.result.sandnode.chain.ClientChainManager;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.message.TauMessageTypes;

public class ConsoleClientChainManager {
    public static void addHandlers(ClientChainManager chainManager, SandnodeClient client) {
        BaseClientChainManager.addHandlers(chainManager, client);

        chainManager.addHandler(TauMessageTypes.FWD, () -> new ConsoleForwardClientChain(client));
        chainManager.addHandler(TauMessageTypes.REACTION, () -> new ConsoleReactionResponseClientChain(client));
    }
}
