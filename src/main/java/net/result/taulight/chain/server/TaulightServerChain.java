package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.error.TauErrors;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.TaulightRequest;
import net.result.taulight.message.types.TaulightResponse;
import net.result.taulight.message.types.TaulightResponse.TaulightResponseData;
import net.result.taulight.db.TauChat;
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
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        while (true) {
            TaulightRequest request = new TaulightRequest(queue.take());

            LOGGER.info(request.getMessageType().name());

            switch (request.getMessageType()) {
                case GET -> {
                    var chats = database.getChats(session.member);
                    send(new TaulightResponse(TaulightResponseData.get(chats)));
                }
                case ADD -> {
                    String chatID = request.getChatID();
                    Optional<TauChat> opt = database.getChat(chatID);
                    if (opt.isEmpty()) {
                        send(TauErrors.CHAT_NOT_FOUND.message());
                        LOGGER.warn("Attempted to add member to a non-existent chat: {}", chatID);
                        return;
                    }

                    TauChat chat = opt.get();

                    if (database.getMembersFromChat(chat).contains(session.member)) {
                        send(new HappyMessage());
                        LOGGER.info("Member {} is already part of chat {}", session.member.getID(), chatID);
                        return;
                    }

                    database.addMemberToChat(chat, session.member);
                    TauAgentProtocol.addMemberToGroup(session, manager.getGroup(chat));

                    send(new HappyMessage());
                    LOGGER.info("Member {} added to chat {}", session.member.getID(), chatID);
                }
            }

            if (request.getHeaders().isFin()) break;
        }
    }
}
