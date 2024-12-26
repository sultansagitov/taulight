package net.result.taulight.chain;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.RequestContextMessage;
import net.result.sandnode.util.IOControl;
import net.result.taulight.messages.types.TextMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForwardClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardClientChain.class);

    public ForwardClientChain(IOControl io) {
        super(io);
    }

    @Override
    public void start() throws InterruptedException {
        send(new RequestContextMessage("fwd"));

        while (io.isConnected()) {
            IMessage request = queue.take();
            LOGGER.info("Forwarded message: {}", new TextMessage(request).data);
        }
    }
}
