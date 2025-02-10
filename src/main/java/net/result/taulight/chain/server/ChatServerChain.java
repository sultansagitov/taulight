package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

            switch (request.getMessageType()) {
                case GET -> {
                    try {
                        send(ChatResponse.get(database.getChats(session.member)));
                    } catch (DatabaseException e) {
                        LOGGER.error(e);
                        send(Errors.SERVER_ERROR.message());
                        continue;
                    }
                }
            }

            if (request.getHeaders().isFin()) break;
        }
    }
}
