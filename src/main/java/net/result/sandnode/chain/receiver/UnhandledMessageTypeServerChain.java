package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnhandledMessageTypeException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnhandledMessageTypeServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UnhandledMessageTypeServerChain.class);

    @Override
    public Message handle(RawMessage raw) {
        if (raw.headers().type() != MessageTypes.ERR) {
            throw new UnhandledMessageTypeException();
        }
        ErrorMessage errorMessage = new ErrorMessage(raw);
        LOGGER.error("Unhandled error from client {}", errorMessage.error.description());
        return null;
    }
}
