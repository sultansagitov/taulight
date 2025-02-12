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
            LOGGER.info("Forwarding message: {}", forwardMessage.getData());

            String chatID = forwardMessage.getChatID();
            if (chatID == null) {
                LOGGER.error("Forward message contains null chatID");
                send(Errors.TOO_FEW_ARGS.message());
                continue;
            }

            Optional<TauChat> tauChat;
            try {
                tauChat = database.getChat(chatID);
            } catch (DatabaseException e) {
                LOGGER.error("Error retrieving chat from database: {}", e.getMessage(), e);
                sendFin(Errors.SERVER_ERROR.message());
                return;
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
                return;
            }

            if (!members.contains(session.member)) {
                LOGGER.warn("Unauthorized access attempt by member: {}", session.member);
                send(Errors.UNAUTHORIZED.message());
                continue;
            }

            ChatMessage chatMessage = new ChatMessage()
                    .setRandomID()
                    .setChatID(forwardMessage.getChatID())
                    .setContent(forwardMessage.getData())
                    .setMemberID(session.member.getID())
                    .setZtd(ztd);

            LOGGER.info("Saving message with id {} content: {}", chatMessage.id(), chatMessage.content());
            try {
                database.saveMessage(chatMessage);
            } catch (DatabaseException e) {
                LOGGER.error("Error saving message to database: {}", e.getMessage(), e);
                sendFin(Errors.SERVER_ERROR.message());
                return;
            }

            boolean forwarded = false;
            for (Session s : manager.getGroup(chat).getSessions()) {
                Optional<Chain> fwd = s.io.chainManager.getChain("fwd");

                ForwardResponse request = new ForwardResponse(chatMessage);

                if (fwd.isEmpty()) {
                    var chain = new ForwardServerChain(s);
                    s.io.chainManager.linkChain(chain);
                    chain.send(request);
                    chain.send(new ChainNameRequest("fwd"));
                } else {
                    fwd.get().send(request);
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
