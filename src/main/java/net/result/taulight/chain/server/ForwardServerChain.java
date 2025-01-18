package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.HappyMessage;
import net.result.sandnode.server.ServerError;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IMember;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauHub;
import net.result.taulight.messages.types.ForwardMessage;
import net.result.taulight.messages.types.TimedForwardMessage;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

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
                LOGGER.error("Failed to find chat with ID: {}", chatID);
                send(ServerError.SERVER_ERROR.message()); // TODO: make own error
                continue;
            }

            TauChat chat = tauChat.get();
            Set<IMember> members = chat.members;

            if (!members.contains(session.member)) {
                send(ServerError.UNAUTHORIZED.message());
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

