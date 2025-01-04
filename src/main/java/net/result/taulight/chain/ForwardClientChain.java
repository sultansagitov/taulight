package net.result.taulight.chain;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exceptions.DeserializationException;
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
    public void sync() throws InterruptedException {
        send(new RequestChainNameMessage("fwd"));

        while (io.isConnected()) {
            RawMessage request = queue.take();
            try {
                TimedForwardMessage forwardMessage = new TimedForwardMessage(request);
                LOGGER.info("Forwarded message: {}", forwardMessage.data);
            } catch (DeserializationException e) {
                LOGGER.error("Deserialization error", e);
                throw new RuntimeException(e);
            }
        }
    }
}
