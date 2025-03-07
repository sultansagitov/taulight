package net.result.sandnode.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnhandledMessageTypeServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UnhandledMessageTypeServerChain.class);

    public UnhandledMessageTypeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, UnprocessedMessagesException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {
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
