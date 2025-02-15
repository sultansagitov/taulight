package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.error.TauErrors;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.ForwardResponse;
import net.result.taulight.db.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        while (true) {
            ForwardRequest forwardMessage = new ForwardRequest(queue.take());

            ZonedDateTime ztd = ZonedDateTime.now(ZoneId.of("UTC"));
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

            Optional<TauChat> tauChat;
            try {
                tauChat = database.getChat(chatID);
            } catch (DatabaseException e) {
                LOGGER.error("Error retrieving chat from database: {}", e.getMessage(), e);
                sendFin(Errors.SERVER_ERROR.message());
                continue;
            }

            if (tauChat.isEmpty()) {
                LOGGER.warn("Attempted to add member to a non-existent chat: {}", chatID);
                send(TauErrors.CHAT_NOT_FOUND.message());
                continue;
            }

            TauChat chat = tauChat.get();
            Collection<Member> members;
            try {
                members = database.getMembersFromChat(chat);
            } catch (DatabaseException e) {
                LOGGER.error("Error retrieving members from chat: {}", e.getMessage(), e);
                sendFin(Errors.SERVER_ERROR.message());
                continue;
            }

            if (!members.contains(session.member)) {
                LOGGER.warn("Unauthorized access attempt by member: {}", session.member);
                send(Errors.UNAUTHORIZED.message());
                continue;
            }

            LOGGER.info("Saving message with id {} content: {}", chatMessage.id(), content);
            try {
                database.saveMessage(chatMessage);
            } catch (DatabaseException e) {
                LOGGER.error("Error saving message to database: {}", e.getMessage(), e);
                sendFin(Errors.SERVER_ERROR.message());
                continue;
            }

            boolean forwarded = false;
            for (Session s : manager.getGroup(chat).getSessions()) {
                Optional<Chain> fwd = s.io.chainManager.getChain("fwd");

                ForwardResponse request = new ForwardResponse(chatMessage, ztd);

                if (fwd.isPresent()) {
                    fwd.get().send(request);
                } else {
                    var chain = new ForwardServerChain(s);
                    s.io.chainManager.linkChain(chain);
                    chain.send(request);
                    chain.send(new ChainNameRequest("fwd"));
                }
                forwarded = true;
            }

            if (!forwarded) {
                LOGGER.warn("Message forwarding failed for chat: {}", chatID);
                send(TauErrors.MESSAGE_NOT_FORWARDED.message());
                continue;
            }

            send(new HappyMessage());
        }
    }
}
