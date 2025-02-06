package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauErrors;
import net.result.taulight.TauHub;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.ChatMessageBuilder;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.ForwardResponse;
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

        boolean isFirst = true;

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

            Optional<TauChat> tauChat = chatManager.find(chatID);
            if (tauChat.isEmpty()) {
                LOGGER.warn("Attempted to add member to a non-existent chat: {}", chatID);
                send(TauErrors.CHAT_NOT_FOUND.message());
                continue;
            }

            TauChat chat = tauChat.get();
            var members = chat.getMembers();

            if (!members.contains(session.member)) {
                LOGGER.warn("Unauthorized access attempt by member: {}", session.member);
                send(Errors.UNAUTHORIZED.message());
                continue;
            }

            ChatMessage chatMessage = new ChatMessageBuilder()
                    .setChatID(forwardMessage.getChatID())
                    .setContent(forwardMessage.getData())
                    .setMemberID(session.member.getID())
                    .setZtd(ztd)
                    .build();

            boolean forwarded = false;
            for (Session s : chat.group.getSessions()) {
                Optional<Chain> fwd = s.io.chainManager.getChain("fwd");

                if (fwd.isEmpty()) {
                    var chain = new ForwardServerChain(session);
                    s.io.chainManager.linkChain(chain);
                    chain.send(new ForwardResponse(chatMessage));
                    forwarded = true;
                    chain.send(new ChainNameRequest("fwd"));
                    continue;
                }

                fwd.get().send(new ForwardResponse(chatMessage));
                forwarded = true;
            }

            if (!forwarded) {
                LOGGER.warn("Message forwarding failed for chat: {}", chatID);
                send(TauErrors.MESSAGE_NOT_FORWARDED.message());
                continue;
            }

            send(new HappyMessage());

            if (isFirst) {
                send(new ChainNameRequest("fwd"));
                isFirst = false;
            }
        }
    }
}
