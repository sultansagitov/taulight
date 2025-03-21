package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
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

public class ChatServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChatServerChain.class);

    public ChatServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        while (true) {
            ChatRequest request = new ChatRequest(queue.take());

            if (session.member == null) {
                send(Errors.UNAUTHORIZED.createMessage());
                continue;
            }

            Collection<UUID> allChatID = request.getAllChatID();
            Collection<ChatInfoProp> chatInfoProps = request.getChatInfoProps();

            try {
                Collection<ChatInfo> infos = new ArrayList<>();

                if (allChatID == null || allChatID.isEmpty()) {
                    for (TauChat chat : database.getChats(session.member)) {
                        if (chat.hasMatchingProps(chatInfoProps)) {
                            infos.add(chat.getInfo(session.member, chatInfoProps));
                        }
                    }
                } else {
                    for (UUID chatID : allChatID) {
                        Optional<TauChat> opt = database.getChat(chatID);
                        if (opt.isEmpty()) {
                            infos.add(ChatInfo.chatNotFound(chatID));
                            continue;
                        }

                        TauChat chat = opt.get();

                        if (!chat.getMembers().contains(session.member)) {
                            infos.add(ChatInfo.chatNotFound(chatID));
                            continue;
                        }

                        if (chat.hasMatchingProps(chatInfoProps)) {
                            infos.add(chat.getInfo(session.member, chatInfoProps));
                        }
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
