package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.error.TauErrors;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.exception.MessageNotForwardedException;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.db.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class ForwardRequestServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardRequestServerChain.class);

    public ForwardRequestServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        while (true) {
            ForwardRequest forwardMessage = new ForwardRequest(queue.take());

            ChatMessage chatMessage = forwardMessage.getChatMessage();

            if (chatMessage == null) {
                LOGGER.error("Forward message contains null chatMessage");
                send(Errors.TOO_FEW_ARGS.message());
                continue;
            }

            UUID chatID = chatMessage.chatID();
            String content = chatMessage.content();

            if (chatID == null || content == null) {
                LOGGER.error("Forward message contains null chatID or content");
                send(Errors.TOO_FEW_ARGS.message());
                continue;
            }

            LOGGER.info("Forwarding message: {}", content);

            chatMessage
                    .setRandomID()
                    .setMemberID(session.member.getID())
                    .setSys(false);

            try {
                Optional<TauChat> chatOpt = database.getChat(chatID);

                if (chatOpt.isEmpty()) {
                    LOGGER.error("Chat was not found");
                    sendFin(TauErrors.CHAT_NOT_FOUND.message());
                    continue;
                }

                TauChat chat = chatOpt.get();

                Collection<Member> members = database.getMembersFromChat(chat);
                if (!members.contains(session.member)) {
                    LOGGER.warn("Unauthorized access attempt by member: {}", session.member);
                    send(Errors.UNAUTHORIZED.message());
                    continue;
                }

                TauHubProtocol.send(session.server.serverConfig, chat, chatMessage);
            } catch (DatabaseException e) {
                LOGGER.error("Database error: {}", e.getMessage(), e);
                sendFin(Errors.SERVER_ERROR.message());
                continue;
            } catch (MessageNotForwardedException e) {
                LOGGER.error("Message forwarding failed for chat: {}", chatID, e);
                send(TauErrors.MESSAGE_NOT_FORWARDED.message());
                continue;
            }

            send(new HappyMessage());
        }
    }

}
