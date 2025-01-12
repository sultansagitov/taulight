package net.result.taulight.chain.server;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.HappyMessage;
import net.result.sandnode.server.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.messages.types.ForwardMessage;
import net.result.taulight.messages.types.TaulightRequestMessage;
import net.result.taulight.messages.types.TaulightResponseMessage;
import net.result.taulight.messages.types.TaulightResponseMessage.TaulightResponseData;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.result.taulight.messages.DataType.ADD;
import static net.result.taulight.messages.DataType.GET;

public class TaulightServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(TaulightServerChain.class);
    private final TauChatManager chatManager;

    public TaulightServerChain(Session session, TauChatManager chatManager) {
        super(session);
        this.chatManager = chatManager;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        while (true) {
            TaulightRequestMessage request = new TaulightRequestMessage(queue.take());

            LOGGER.info(request.getMessageType().name());


            switch (request.getMessageType()) {
                case GET -> {
                    Set<String> set = chatManager
                            .getChats(session.member).stream()
                            .map(chat -> chat.name)
                            .collect(Collectors.toSet());
                    send(new TaulightResponseMessage(new TaulightResponseData(GET, set)));
                }
                case ADD -> {
                    chatManager.find("first").ifPresent(chat -> {
                        chatManager.addMember(chat, session.member);
                    });
                    send(new TaulightResponseMessage(new TaulightResponseData(ADD)));
                }
                case REMOVE -> {
                }
                case WRITE -> {
                    Optional<TauChat> first = chatManager.find("first");
                    if (first.isPresent()) {
                        for (Session s : session.server.node.agentSessionList) {
                            if (first.get().members.contains(s.member)) {
                                Optional<Chain> fwd = s.chainManager.getChain("fwd");
                                if (fwd.isPresent()) {
                                    fwd.get().send(new ForwardMessage(request.object.message));
                                }
                            }
                        }
                    }

                    send(new HappyMessage());
                }
            }




            if (request.getHeaders().isFin()) break;
        }
    }
}
