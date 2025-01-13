package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.RequestChainNameMessage;
import net.result.sandnode.util.IOControl;
import net.result.taulight.messages.types.TimedForwardMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
                LOGGER.info("{} ended by interrupting", toString());
                break;
            }

            if (request.getHeaders().isFin()) {
                LOGGER.info("{} ended by FIN flag in received message", toString());
                break;
            }

            TimedForwardMessage tfm = new TimedForwardMessage(request);

            ZonedDateTime zonedDateTime = tfm.getZonedDateTime();

            ZonedDateTime localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
            String formattedDateTime = localZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

            LOGGER.info("Forwarded message details - local time: {}, chatID: {}, data: {}",
                    formattedDateTime, tfm.getChatID(), tfm.getData());

        }
    }
}
