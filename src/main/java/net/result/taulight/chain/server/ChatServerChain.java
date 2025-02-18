package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TauDirect;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

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

            ChatRequest.DataType messageType = request.getMessageType();

            try {
                switch (messageType) {
                    case GET -> send(ChatResponse.get(database.getChats(session.member)));
                    case INFO -> {
                        Collection<UUID> allChatID = request.getAllChatID();

                        Collection<ChatResponse.Info> infos = new ArrayList<>();
                        for (UUID chatID : allChatID) {
                            Optional<TauChat> opt = database.getChat(chatID);

                            if (opt.isEmpty()) {
                                infos.add(ChatResponse.Info.chatNotFound(chatID));
                                continue;
                            }

                            TauChat chat = opt.get();

                            Collection<Member> members = database.getMembersFromChat(chat);
                            if (!members.contains(session.member)) {
                                infos.add(ChatResponse.Info.chatNotFound(chatID));
                                continue;
                            }

                            if (chat instanceof TauChannel channel) {
                                infos.add(ChatResponse.Info.channel(channel));
                                continue;
                            }

                            if (chat instanceof TauDirect direct) {
                                infos.add(ChatResponse.Info.direct(direct, direct.otherMember(session.member)));
                            }
                        }

                        send(ChatResponse.infos(infos));
                    }
                }
            } catch (DatabaseException e) {
                LOGGER.error(e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            }

            if (request.headers().fin()) break;
        }
    }
}
