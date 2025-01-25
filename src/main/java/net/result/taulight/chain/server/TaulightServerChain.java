package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauHub;
import net.result.taulight.message.types.TaulightRequestMessage;
import net.result.taulight.message.types.TaulightResponseMessage;
import net.result.taulight.message.types.TaulightResponseMessage.TaulightResponseData;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import static net.result.taulight.message.DataType.ADD;
import static net.result.taulight.message.DataType.GET;

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
                    var set = chatManager
                            .getChats(session.member).stream()
                            .map(TauChat::getID)
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
