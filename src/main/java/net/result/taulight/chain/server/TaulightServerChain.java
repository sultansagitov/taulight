package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.server.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauHub;
import net.result.taulight.messages.types.TaulightRequestMessage;
import net.result.taulight.messages.types.TaulightResponseMessage;
import net.result.taulight.messages.types.TaulightResponseMessage.TaulightResponseData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.stream.Collectors;

import static net.result.taulight.messages.DataType.ADD;
import static net.result.taulight.messages.DataType.GET;

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
                    Set<String> set = chatManager
                            .getChats(session.member).stream()
                            .map(chat -> chat.name)
                            .collect(Collectors.toSet());
                    send(new TaulightResponseMessage(new TaulightResponseData(GET, set)));
                }
                case ADD -> {
                    chatManager.find("first").ifPresent(chat -> chatManager.addMember(chat, session.member));
                    send(new TaulightResponseMessage(new TaulightResponseData(ADD)));
                }
                case REMOVE -> {
                }
            }

            if (request.getHeaders().isFin()) break;
        }
    }
}
