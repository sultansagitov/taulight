package net.result.main.chain.client;

import net.result.sandnode.util.IOController;
import net.result.taulight.chain.client.ForwardClientChain;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ConsoleForwardClientChain extends ForwardClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardClientChain.class);

    public ConsoleForwardClientChain(IOController io) {
        super(io);
    }

    @Override
    public void onMessage(ForwardResponse tfm) {
        var zonedDateTime = tfm.getServerZonedDateTime();
        var localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        var formatted = localZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

        String content = tfm.getChatMessage().content();
        LOGGER.info("Forwarded message details - {} - {} - {}", tfm.getChatMessage().id(), formatted, content);
        System.out.printf("%s > %s > %s%n", tfm.getChatMessage().chatID(), tfm.getChatMessage().memberID(), content);
    }
}
