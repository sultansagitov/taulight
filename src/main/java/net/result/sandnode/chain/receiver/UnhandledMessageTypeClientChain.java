package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.exception.error.UnhandledMessageTypeException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class UnhandledMessageTypeClientChain extends ClientChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UnhandledMessageTypeClientChain.class);

    public UnhandledMessageTypeClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public @Nullable IMessage handle(RawMessage raw) throws Exception {
        LOGGER.error(raw);
        if (raw.headers().type() != MessageTypes.ERR) {
            throw new UnhandledMessageTypeException();
        }
        ErrorMessage errorMessage = new ErrorMessage(raw);
        LOGGER.error("Unhandled error from server {}", errorMessage.error.description());
        return null;
    }
}
