package net.result.main.chain.receiver;

import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.chain.receiver.ForwardClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ConsoleForwardClientChain extends ForwardClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardClientChain.class);

    public ConsoleForwardClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public void onMessage(ChatMessageViewDTO serverMessage, String decrypted, boolean yourSession) {
        String formatted = serverMessage.creationDate
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

        ChatMessageInputDTO message = serverMessage.message;
        LOGGER.info("Forwarded message details - {} - {} - {}", serverMessage.id, formatted, decrypted);
        String nickname = yourSession ? "You" : message.nickname;
        System.out.printf("%s > %s > %s%n", message.chatID, nickname, decrypted);
    }
}
