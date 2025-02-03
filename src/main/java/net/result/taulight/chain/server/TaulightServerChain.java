package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauErrors;
import net.result.taulight.TauHub;
import net.result.taulight.message.types.TaulightRequestMessage;
import net.result.taulight.message.types.TaulightResponseMessage;
import net.result.taulight.message.types.TaulightResponseMessage.TaulightResponseData;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class TaulightServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(TaulightServerChain.class);

    public TaulightServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        while (true) {
            TaulightRequestMessage request = new TaulightRequestMessage(queue.take());

            LOGGER.info(request.getMessageType().name());

            TauHub tauHub = (TauHub) session.server.node;
            TauChatManager chatManager = tauHub.chatManager;

            switch (request.getMessageType()) {
                case GET -> {
                    var chats = chatManager.getChats(session.member);
                    send(new TaulightResponseMessage(TaulightResponseData.get(chats)));
                }
                case ADD -> {
                    String chatID = request.getChatID();
                    Optional<TauChat> opt = chatManager.find(chatID);
                    if (opt.isEmpty()) {
                        send(TauErrors.CHAT_NOT_FOUND.message());
                        LOGGER.warn("Attempted to add member to a non-existent chat: {}", chatID);
                        return;
                    }

                    TauChat chat = opt.get();

                    if (chat.getMembers().contains(session.member)) {
                        send(new HappyMessage());
                        LOGGER.info("Member {} is already part of chat {}", session.member.getID(), chatID);
                        return;
                    }

                    chatManager.addMember(chat, session.member);

                    send(new HappyMessage());
                    LOGGER.info("Member {} added to chat {}", session.member.getID(), chatID);
                }
                case REMOVE -> {
                }
            }

            if (request.getHeaders().isFin()) break;
        }
    }
}
