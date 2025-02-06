package net.result.main.chain.client;

import net.result.sandnode.util.IOController;
import net.result.taulight.chain.client.ForwardClientChain;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleForwardClientChain extends ForwardClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardClientChain.class);

    public ConsoleForwardClientChain(IOController io) {
        super(io);
    }

    @Override
    public void onMessage(ForwardResponse tfm) {
        ZonedDateTime zonedDateTime = tfm.getZonedDateTime();

        ZonedDateTime localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedDateTime = localZonedDateTime.format(formatter);

        LOGGER.info("Forwarded message details - member: {} time: {}, chatID: {}, data: {}",
                tfm.getMember().memberID, formattedDateTime, tfm.getChatID(), tfm.getData());
    }
}
