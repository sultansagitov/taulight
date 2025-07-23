package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import org.jetbrains.annotations.Nullable;

public class ExitClientChain extends ClientChain implements ReceiverChain {
    public ExitClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public @Nullable Message handle(RawMessage raw) throws Exception {
        client.io().disconnect(false);
        return null;
    }
}
