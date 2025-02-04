package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauErrors;
import net.result.taulight.TauHub;
import net.result.taulight.message.types.ForwardMessage;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.TimedForwardMessage;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ForwardRequestServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardRequestServerChain.class);

    public ForwardRequestServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        TauHub tauHub = (TauHub) session.server.node;
        TauChatManager chatManager = tauHub.chatManager;

        new ForwardRequest(queue.take());

        while (io.connected) {
            ForwardMessage forwardMessage = new ForwardMessage(queue.take());

            ZonedDateTime ztd = ZonedDateTime.now(ZoneId.of("UTC"));
            LOGGER.info("Forwarding message: {}", forwardMessage.getData());

            String chatID = forwardMessage.getChatID();
            if (chatID == null) {
                LOGGER.error("Forward message contains null chatID");
                continue;
            }

            Optional<TauChat> tauChat = chatManager.find(chatID);

            if (tauChat.isEmpty()) {
                send(TauErrors.CHAT_NOT_FOUND.message());
                LOGGER.warn("Attempted to add member to a non-existent chat: {}", chatID);
                continue;
            }

            TauChat chat = tauChat.get();
            var members = chat.getMembers();

            if (!members.contains(session.member)) {
                send(Errors.UNAUTHORIZED.message());
                continue;
            }

            for (Session s : chat.group.getSessions()) {
                Optional<Chain> fwd = s.io.chainManager.getChain("fwd");

                if (fwd.isEmpty()) {
                    LOGGER.warn("Failed to find forwarding chain for session: {}", s);
                    continue;
                }

                fwd.get().send(new TimedForwardMessage(forwardMessage, ztd, session.member));
                LOGGER.info("Message forwarded to session: {}", s);
            }

            send(new HappyMessage());
        }
    }
}
