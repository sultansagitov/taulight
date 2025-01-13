package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.HappyMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IMember;
import net.result.sandnode.util.group.GroupManager;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauHub;
import net.result.taulight.messages.OnlineResponseMessage;
import net.result.taulight.messages.types.TaulightRequestMessage;
import net.result.taulight.messages.types.TaulightResponseMessage;
import net.result.taulight.messages.types.TaulightResponseMessage.TaulightResponseData;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.result.sandnode.server.ServerError.SERVER_ERROR;
import static net.result.taulight.messages.TauMessageTypes.ONL;
import static net.result.taulight.messages.TauMessageTypes.TAULIGHT;

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
                        Set<TauChat> chats = chatManager.getChats(session.member);
                        send(new TaulightResponseMessage(TaulightResponseData.get(chats)));
                    }
                    case ADD -> {
                        String chatId = taulightRequest.getChatID();
                        Optional<TauChat> opt = chatManager.find(chatId);
                        if (opt.isEmpty()) {
                            send(SERVER_ERROR.message()); // TODO: make own error
                            LOGGER.warn("Attempted to add member to a non-existent chat: {}", chatId);
                            return;
                        }

                        TauChat chat = opt.get();

                        if (chat.members.contains(session.member)) {
                            send(new HappyMessage());
                            LOGGER.info("Member {} is already part of chat {}", session.member.getID(), chatId);
                            return;
                        }

                        chatManager.addMember(chat, session.member);

                        send(new HappyMessage());
                        LOGGER.info("Member {} added to chat {}", session.member.getID(), chatId);
                    }

                }
            }

            if (request.getHeaders().getType() == ONL) {
                final GroupManager groupManager = session.server.serverConfig.groupManager();
                Set<Session> fwd = groupManager.getGroup("chat").getSessions();
                Set<IMember> list = fwd.stream().map(s -> s.member).collect(Collectors.toSet());
                LOGGER.info("Online IPs: {}", list);
                OnlineResponseMessage response = new OnlineResponseMessage(list);
                send(response);
            }
        }
    }
}