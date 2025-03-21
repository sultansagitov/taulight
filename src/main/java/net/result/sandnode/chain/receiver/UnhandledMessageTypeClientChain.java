package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.sender.ClientChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnhandledMessageTypeClientChain extends ClientChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UnhandledMessageTypeClientChain.class);

    public UnhandledMessageTypeClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws Exception {
        RawMessage raw = queue.take();

        LOGGER.error(raw);
        if (raw.headers().type() != MessageTypes.ERR) {
            sendFin(Errors.UNHANDLED_MESSAGE_TYPE.createMessage());
            return;
        }
        ErrorMessage errorMessage = new ErrorMessage(raw);
        ServerErrorManager.instance().throwAll(errorMessage.error);
    }
}
