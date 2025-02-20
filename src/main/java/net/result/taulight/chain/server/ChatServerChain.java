package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ChatServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(ChatServerChain.class);

    public ChatServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        while (true) {
            ChatRequest request = new ChatRequest(queue.take());

            Collection<UUID> allChatID = request.getAllChatID();
            Collection<ChatInfoProp> chatInfoProps = request.getChatInfoProps();

            try {
                Collection<ChatInfo> infos = new ArrayList<>();

                if (allChatID == null || allChatID.isEmpty()) {
                    for (TauChat chat : database.getChats(session.member)) {
                        infos.add(ChatInfo.byChat(chat, session.member, chatInfoProps));
                    }
                } else {
                    for (UUID chatID : allChatID) {
                        Optional<TauChat> opt = database.getChat(chatID);
                        if (opt.isEmpty()) {
                            infos.add(ChatInfo.chatNotFound(chatID));
                            continue;
                        }

                        TauChat chat = opt.get();

                        if (!database.getMembersFromChat(chat).contains(session.member)) {
                            infos.add(ChatInfo.chatNotFound(chatID));
                            continue;
                        }

                        infos.add(ChatInfo.byChat(chat, session.member, chatInfoProps));
                    }
                }

                send(new ChatResponse(infos));
            } catch (DatabaseException e) {
                LOGGER.error(e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            }

            if (request.headers().fin()) break;
        }
    }
}
