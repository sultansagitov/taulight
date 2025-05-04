package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ForwardClientChain extends ClientChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardClientChain.class);

    public ForwardClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws DeserializationException, ExpectedMessageException {
        while (io.connected) {
            RawMessage request;
            try {
                request = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} is ended", this);
                break;
            }

            onMessage(new ForwardResponse(request));

            if (request.headers().fin()) {
                LOGGER.info("{} ended by FIN flag in received message", toString());
                break;
            }
        }
    }

    public abstract void onMessage(ForwardResponse tfm);
}
