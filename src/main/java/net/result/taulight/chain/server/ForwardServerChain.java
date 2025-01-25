package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.error.Errors;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauHub;
import net.result.taulight.TauErrors;
import net.result.taulight.message.types.ForwardMessage;
import net.result.taulight.message.types.TimedForwardMessage;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ForwardServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardServerChain.class);

    public ForwardServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        TauHub tauHub = (TauHub) session.server.node;
        TauChatManager chatManager = tauHub.chatManager;

        while (io.isConnected()) {
            RawMessage request = queue.take();

            ForwardMessage forwardMessage = new ForwardMessage(request);

            ZonedDateTime ztd = ZonedDateTime.now(ZoneId.of("UTC"));
            LOGGER.info("Forwarding message: {}", forwardMessage.getData());

            String chatID = forwardMessage.getChatID();
            if (chatID == null) {
                LOGGER.error("Forward message contains null chatID");
                return;
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

            for (Session s : tauHub.agentSessionList) {
                if (!members.contains(s.member)) continue;

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

