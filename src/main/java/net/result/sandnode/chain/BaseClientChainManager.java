package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.ExitClientChain;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;

public class BaseClientChainManager extends BaseChainManager implements ClientChainManager {
    public static void addHandlers(ClientChainManager chainManager, SandnodeClient client) {
        chainManager.addHandler(MessageTypes.EXIT, () -> new ExitClientChain(client));
    }
}
