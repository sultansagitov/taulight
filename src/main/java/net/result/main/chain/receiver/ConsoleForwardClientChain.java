package net.result.main.chain.receiver;

import net.result.sandnode.util.IOController;
import net.result.taulight.chain.receiver.ForwardClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ConsoleForwardClientChain extends ForwardClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardClientChain.class);

    public ConsoleForwardClientChain(IOController io) {
        super(io);
    }

    @Override
    public void onMessage(@NotNull ForwardResponse response) {
        ChatMessageViewDTO serverMessage = response.getServerMessage();

        String formatted = serverMessage.getCreationDate()
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

        ChatMessageInputDTO message = serverMessage.message();
        String content = message.content();
        LOGGER.info("Forwarded message details - {} - {} - {}", serverMessage.id(), formatted, content);
        String nickname = response.isYourSession() ? "You" : message.nickname();
        System.out.printf("%s > %s > %s%n", message.chatID(), nickname, content);
    }
}
