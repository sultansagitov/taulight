package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.group.GroupManager;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauErrors;
import net.result.taulight.TauHub;
import net.result.taulight.message.types.OnlineResponseMessage;
import net.result.taulight.message.types.TaulightRequestMessage;
import net.result.taulight.message.types.TaulightResponseMessage;
import net.result.taulight.message.types.TaulightResponseMessage.TaulightResponseData;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.stream.Collectors;

import static net.result.taulight.message.TauMessageTypes.ONL;
import static net.result.taulight.message.TauMessageTypes.TAULIGHT;

public class GetOnlineServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(GetOnlineServerChain.class);

    public GetOnlineServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        TauHub tauHub = (TauHub) session.server.node;
        TauChatManager chatManager = tauHub.chatManager;

        while (io.isConnected()) {
            RawMessage request = queue.take();

            if (request.getHeaders().getType() == TAULIGHT) {
                TaulightRequestMessage taulightRequest = new TaulightRequestMessage(request);
                switch (taulightRequest.getMessageType()) {
                    case GET -> {
                        var chats = chatManager.getChats(session.member);
                        send(new TaulightResponseMessage(TaulightResponseData.get(chats)));
                    }
                    case ADD -> {
                        String chatID = taulightRequest.getChatID();
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

                }
            }

            if (request.getHeaders().getType() == ONL) {
                final GroupManager groupManager = session.server.serverConfig.groupManager();
                var fwd = groupManager.getGroup("chat").getSessions();
                var list = fwd.stream().map(s -> s.member).collect(Collectors.toSet());
                LOGGER.info("Online IPs: {}", list);
                OnlineResponseMessage response = new OnlineResponseMessage(list);
                send(response);
            }
        }
    }
}