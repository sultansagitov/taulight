package net.result.taulight.chain;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.RequestChainNameMessage;
import net.result.sandnode.util.IOControl;
import net.result.taulight.messages.types.TimedForwardMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForwardClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardClientChain.class);

    public ForwardClientChain(IOControl io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException {
        send(new RequestChainNameMessage("fwd"));

        while (true) {
            RawMessage request;
            try {
                request = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} cid={} ended by interrupting", getClass().getSimpleName(), getID());
                break;
            }

            if (request.getHeaders().isFin()) {
                LOGGER.info("{} cid={} ended by FIN flag in received message", getClass().getSimpleName(), getID());
                break;
            }

            TimedForwardMessage forwardMessage = new TimedForwardMessage(request);
            LOGGER.info("Forwarded message: {}", forwardMessage.getData());
        }
    }
}
