package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RequestChainNameMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.ForwardMessage;
import net.result.taulight.message.types.TimedForwardMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ForwardClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardClientChain.class);

    public ForwardClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException {
        send(new ForwardMessage(new ForwardMessage.ForwardData()));
        send(new RequestChainNameMessage("fwd"));

        while (io.connected) {
            RawMessage request;
            try {
                request = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} is ended", this);
                break;
            }

            if (request.getHeaders().isFin()) {
                LOGGER.info("{} ended by FIN flag in received message", toString());
                break;
            }

            onMessage(new TimedForwardMessage(request));
        }
    }

    public void onMessage(TimedForwardMessage tfm) {
        ZonedDateTime zonedDateTime = tfm.getZonedDateTime();

        ZonedDateTime localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedDateTime = localZonedDateTime.format(formatter);

        LOGGER.info("Forwarded message details - member: {} time: {}, chatID: {}, data: {}",
                tfm.getMember().memberID, formattedDateTime, tfm.getChatID(), tfm.getData());
    }
}
